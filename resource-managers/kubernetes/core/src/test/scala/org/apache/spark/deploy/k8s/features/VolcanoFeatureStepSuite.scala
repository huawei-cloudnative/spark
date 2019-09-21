/*
 * Copyright 2019 The Volcano Authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.spark.deploy.k8s.features

import sh.volcano.scheduling.PodGroup

import org.apache.spark.{SparkConf, SparkFunSuite}
import org.apache.spark.deploy.k8s._

class VolcanoFeatureStepSuite extends SparkFunSuite {
  private val sparkConf = new SparkConf(false)
    .set("spark.kubernetes.volcano.enable", "true")
  private val emptyKubernetesConf = KubernetesConf(
    sparkConf = sparkConf,
    roleSpecificConf = KubernetesDriverSpecificConf(
      None,
      "app-name",
      "main",
      Seq.empty),
    appResourceNamePrefix = "resource",
    appId = "app-id",
    roleLabels = Map.empty,
    roleAnnotations = Map.empty,
    roleSecretNamesToMountPaths = Map.empty,
    roleSecretEnvNamesToKeyRefs = Map.empty,
    roleEnvs = Map.empty,
    roleVolumes = Nil,
    sparkFiles = Nil)

  test("assign pod with volcano") {
    val kubernetesConf = emptyKubernetesConf.copy()
    val step = new VolcanoFeatureStep(kubernetesConf)
    val configuredPod = step.configurePod(SparkPod.initialPod())

    val annotation = configuredPod.pod.getMetadata().getAnnotations

    assert(configuredPod.pod.getSpec.getSchedulerName === "volcano")
    assert(annotation.get("volcano.sh/task-spec") === "spark-driver")
  }

  test("support podgroup") {
    val kubernetesConf = emptyKubernetesConf.copy()
    val step = new VolcanoFeatureStep(kubernetesConf)
    val configuredPod = step.configurePod(SparkPod.initialPod())
    val resources = step.getAdditionalKubernetesResources()
    val annotation = configuredPod.pod.getMetadata().getAnnotations

    assert(resources.length == 1)
    val podgroup = resources.head.asInstanceOf[PodGroup]

    assert(annotation.get("scheduling.k8s.io/group-name") === podgroup.getMetadata().getName)
    assert(podgroup.getSpec().getQueue === "default")
  }
}
