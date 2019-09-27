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

import java.util

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import io.fabric8.kubernetes.api.model.{HasMetadata, ObjectMeta, PodBuilder, Quantity, QuantityBuilder}
import sh.volcano.scheduling.{PodGroup, PodGroupSpec, PodGroupStatus, v1alpha1}
import org.apache.spark.deploy.k8s.{KubernetesConf, KubernetesDriverSpecificConf, KubernetesRoleSpecificConf, KubernetesUtils, SparkPod}
import org.apache.spark.deploy.k8s.Config._

private[spark] class VolcanoFeatureStep(
    kubernetesConf: KubernetesConf[_ <: KubernetesRoleSpecificConf])
  extends KubernetesFeatureConfigStep {

  private val conf = kubernetesConf.sparkConf
  private val podGroupName = s"${kubernetesConf.appResourceNamePrefix}-podgroup"
  private val deployModeKey = "spark.submit.deployMode"
  private val clusterMode = "cluster"

  override def configurePod(pod: SparkPod): SparkPod = {
    val isDriver = kubernetesConf.roleSpecificConf.isInstanceOf[KubernetesDriverSpecificConf]
    val roleName = if (isDriver) v1alpha1.VOLCANO_ROLE_DRIVER else v1alpha1.VOLCANO_ROLE_EXECUTOR
    val isClusterMode = (clusterMode == kubernetesConf.sparkConf.get(deployModeKey, clusterMode))

    val schedulerName = conf.get(KUBERNETES_VOLCANO_SCHEDULER_NAME)
    val k8sPodBuilder = new PodBuilder(pod.pod)
      .editMetadata()
        .addToAnnotations(v1alpha1.VOLCANO_TASK_SPEC, roleName)
      .endMetadata()
      .editSpec()
        .withSchedulerName(schedulerName)
      .endSpec()

      k8sPodBuilder.editMetadata()
        .addToAnnotations(v1alpha1.POD_GROUP_ANNOTATION, podGroupName)
      .endMetadata()

    val k8sPod = k8sPodBuilder.build()
    SparkPod(k8sPod, pod.container)
  }

  override def getAdditionalPodSystemProperties(): Map[String, String] = Map.empty

  private def getPodResources(): Map[String, Quantity] = {
    val cpu = conf.get(KUBERNETES_VOLCANO_PODGROUP_CPU)
    val memory = conf.get(KUBERNETES_VOLCANO_PODGROUP_MEMORY)

    val cpuQ = new QuantityBuilder(false)
      .withAmount(s"${cpu}")
      .build()
    val memoryQ = new QuantityBuilder(false)
      .withAmount(s"${memory}Mi")
      .build()

    Map("cpu" -> cpuQ, "memory" -> memoryQ)
  }

  override def getAdditionalKubernetesResources(): Seq[HasMetadata] = {
    val podGroupAnnotations = KubernetesUtils.parsePrefixedKeyValuePairs(
      conf, KUBERNETES_VOLCANO_PODGROUP_ANNOTATION_PREFIX)
    val queue = conf.get(KUBERNETES_VOLCANO_QUEUE)

    val meta = new ObjectMeta()
    meta.setAnnotations(podGroupAnnotations.asJava)
    meta.setName(podGroupName)
    meta.setNamespace(kubernetesConf.namespace())

    val podGroupSpec = new PodGroupSpec()
    podGroupSpec.setMinMember(1)
    podGroupSpec.setQueue(queue)
    podGroupSpec.setMinResources(getPodResources().asJava)

    val podGroupStatus = new PodGroupStatus()
    podGroupStatus.setPhase("Pending")

    val podGroup = new PodGroup()
    podGroup.setMetadata(meta)
    podGroup.setSpec(podGroupSpec)
    podGroup.setStatus(podGroupStatus)

    Seq{podGroup}
  }
}
