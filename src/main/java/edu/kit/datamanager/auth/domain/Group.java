/*
 * Copyright 2017 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.auth.domain;

import edu.kit.dama.Constants;
import edu.kit.dama.entities.dc40.Agent;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author jejkal
 */
@ApiModel(description = "An agent of type 'group'.")
public class Group extends Agent {

    public static final String GROUP_AGENT_TYPE = "GROUP";

    private static Group SYSTEM = null;
    private static Group ANONYMOUS = null;

    @ApiModelProperty(example = "Sample group for testing.", dataType = "String", required = false)
    private String description;
    @ApiModelProperty(value = "List of RepositoryAgents who are group members. This list may only contain agent identifiers.", required = true)
    private Set<String> memberIdentifiers = new HashSet<>();

    public static final synchronized Group getSystemGroup() {
        if (SYSTEM == null) {
            SYSTEM = new Group();
            SYSTEM.setIdentifier(Constants.SYSTEM_GROUP_ID);
            SYSTEM.setName("System");
            SYSTEM.getMemberIdentifiers().add(Constants.SYSTEM_USER_ID);
        }
        return SYSTEM;
    }

    public static final synchronized Group getAnonymousGroup() {
        if (ANONYMOUS == null) {
            ANONYMOUS = new Group();
            ANONYMOUS.setIdentifier(Constants.ANONYMOUS_GROUP_ID);
            ANONYMOUS.setName("Anonymous");
            ANONYMOUS.setAgentType(GROUP_AGENT_TYPE);
            ANONYMOUS.getMemberIdentifiers().add(Constants.ANONYMOUS_USER_ID);
        }
        return ANONYMOUS;
    }

    public Group() {
        setAgentType(GROUP_AGENT_TYPE);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getMemberIdentifiers() {
        return memberIdentifiers;
    }

    public void setMemberIdentifiers(Set<String> memberIdentifiers) {
        this.memberIdentifiers = memberIdentifiers;
    }

    @Override
    public final void setAgentType(String agentType) {
        super.setAgentType(GROUP_AGENT_TYPE);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;

        if (this.isDisabled() != other.isDisabled()) {
            return false;
        }
        if (this.getExpiresAt() != other.getExpiresAt()) {
            return false;
        }
        if (!Objects.equals(this.getName(), other.getName())) {
            return false;
        }
        if (!Objects.equals(this.getIdentifier(), other.getIdentifier())) {
            return false;
        }
        return Objects.equals(this.description, other.description);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.getName());
        hash = 31 * hash + Objects.hashCode(this.getIdentifier());
        hash = 31 * hash + (this.isDisabled() ? 1 : 0);
        hash = 31 * hash + Objects.hashCode(this.getExpiresAt());
        hash = 31 * hash + Objects.hashCode(this.getDescription());
        return hash;
    }
}
