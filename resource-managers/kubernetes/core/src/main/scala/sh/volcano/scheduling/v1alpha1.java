/**
 * Copyright 2019 The Volcano Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sh.volcano.scheduling;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import io.fabric8.kubernetes.client.Handlers;

/**
 */
public class v1alpha1 {
  private static String resourceScope(boolean resourceNamespaced) {
    if (resourceNamespaced) {
      return "Namespaced";
    }
    return "Cluster";
  }

  public static final String POD_GROUP_ANNOTATION = "scheduling.k8s.io/group-name";
  public static final String VOLCANO_TASK_SPEC = "volcano.sh/task-spec";
  public static final String VOLCANO_ROLE_DRIVER = "spark-driver";
  public static final String VOLCANO_ROLE_EXECUTOR = "spark-executor";

  public static final String GROUP = "scheduling.incubator.k8s.io";
  public static final String POD_GROUP_CRD_NAME = "podgroups." + GROUP;
  public static final String POD_GROUP_KIND = "PodGroup";
  public static final String POD_GROUP_LIST_KIND = "PodGroupList";
  public static final String POD_GROUP_PLURAL = "podgroups";
  public static final String POD_GROUP_SINGULAR = "podgroup";
  public static final String POD_GROUP_APIVERSION = "v1alpha1";
  public static final String POD_GROUP_GROUP_APIVERSION = GROUP + "/" + POD_GROUP_APIVERSION;

  public static final CustomResourceDefinition PodGroupCRD = new CustomResourceDefinitionBuilder().
  withApiVersion("apiextensions.k8s.io/v1beta1").
  withNewMetadata().withName(POD_GROUP_CRD_NAME).endMetadata().
  withNewSpec().
    withGroup(GROUP).withVersion(POD_GROUP_APIVERSION).withScope(resourceScope(true)).
    withNewNames().withKind(POD_GROUP_KIND).withListKind(POD_GROUP_LIST_KIND).
    withPlural(POD_GROUP_PLURAL).withSingular(POD_GROUP_SINGULAR).endNames().
  endSpec().
  build();

  public static NonNamespaceOperation<PodGroup, PodGroupList, DoneablePodGroup, Resource<PodGroup, DoneablePodGroup>> getClient(KubernetesClient client, String namespace) {
    NonNamespaceOperation<PodGroup, PodGroupList, DoneablePodGroup, Resource<PodGroup, DoneablePodGroup>> podGroupClient = client.customResources(PodGroupCRD, PodGroup.class, PodGroupList.class, DoneablePodGroup.class);
    podGroupClient = ((MixedOperation<PodGroup, PodGroupList, DoneablePodGroup, Resource<PodGroup, DoneablePodGroup>>) podGroupClient).inNamespace(namespace);
    return podGroupClient;
  }

  {
    KubernetesDeserializer.registerCustomKind(POD_GROUP_GROUP_APIVERSION, POD_GROUP_KIND, PodGroup.class);
    KubernetesDeserializer.registerCustomKind(POD_GROUP_GROUP_APIVERSION, POD_GROUP_LIST_KIND, PodGroupList.class);
  }
}
