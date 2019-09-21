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

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.Quantity;

/**
 */
@JsonDeserialize(
    using = JsonDeserializer.None.class
)
public class PodGroupSpec implements KubernetesResource {
  private int minMember;
  private String queue;
  private String priorityClassName;
  private Map<String, Quantity> minResources = new HashMap<String, Quantity>();

  @Override
  public String toString() {
    return "PodGroupSpec{" +
            "minMember='" + minMember + '\'' +
            ", queue='" + queue + '\'' +
            ", priorityClassName='" + priorityClassName + '\'' +
            ", minResources='" + minResources + '\'' +
        '}';
  }

  public int getMinMember() {
    return minMember;
  }

  public void setMinMember(int minMember) {
    this.minMember = minMember;
  }

  public String getQueue() {
    return queue;
  }

  public void setQueue(String queue) {
    this.queue = queue;
  }

  public String getPriorityClassName() {
    return priorityClassName;
  }

  public void setPriorityClassName(String priorityClassName) {
    this.priorityClassName = priorityClassName;
  }

  public void addToMinResources(String resource, Quantity value) { minResources.put(resource, value); };

  public void addToMinResources(Map<String, Quantity> resources) { minResources.putAll(resources); };

  public void removeFromMinResources(String resource) { minResources.remove(resource); };

  public void removeFromMinResources(Map<String, Quantity> resources) {
    for (String resource: resources.keySet()) {
      minResources.remove(resource);
    }
  };

  public Map<String, Quantity> getMinResources() { return minResources; };

  public void setMinResources(Map<String, Quantity> resources) { minResources = resources; };

  public Boolean hasMinResources() { return minResources.size() > 0; };

}
