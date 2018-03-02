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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.xml.bind.annotation.XmlTransient;
import edu.kit.dama.entities.BaseEntity;
import edu.kit.dama.entities.dc40.exceptions.InvalidResourceException;
/**
 *
 * @author jejkal
 */
@ApiModel(description = "An agent accessing resources, e.g. a user or group.")
public class Agent extends BaseEntity {

    @JsonIgnore
    private String _key;

    @ApiModelProperty(value = "Doe, John", dataType = "String", required = true)
    private String name;
    @ApiModelProperty(value = "0000-0003-2804-688X", dataType = "String", required = false)
    private String identifier;
    @ApiModelProperty(required = false)
    private Scheme identifierScheme;

    //flag for disabling users
    @ApiModelProperty(dataType = "Boolean", example = "false", required = false)
    @XmlTransient
    private boolean disabled = false;
    //timestamp when the user expires, e.g. for temporary users.
    @ApiModelProperty(value = "Date at which the group expires, e.g. after which it is disabled.", example = "2020-05-16'T'13:09:12.000'Z'", required = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "GMT")
    @XmlTransient
    private java.util.Date expiresAt = null;

    private String agentType = "ABSTRACT";

    public Agent() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        _key = identifier;
    }

    public Scheme getIdentifierScheme() {
        return identifierScheme;
    }

    public void setIdentifierScheme(Scheme identifierScheme) {
        this.identifierScheme = identifierScheme;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setExpiresAt(java.util.Date expiresAt) {
        this.expiresAt = expiresAt;
    }

    public java.util.Date getExpiresAt() {
        return expiresAt;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = 41 * hash + Objects.hashCode(this.identifier);
        hash = 41 * hash + Objects.hashCode(this.identifierScheme);
        hash = 41 * hash + (this.disabled ? 1 : 0);
        hash = 41 * hash + Objects.hashCode(this.expiresAt);
        return hash;
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
        final Agent other = (Agent) obj;
        if (this.disabled != other.disabled) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if (!Objects.equals(this.identifierScheme, other.identifierScheme)) {
            return false;
        }
        return Objects.equals(this.expiresAt, other.expiresAt);
    }

//    @Override
//    public String toString() {
//        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
//    }

    @Override
    public String getId() {
        return _key;
    }

    @Override
    public void validate() throws InvalidResourceException {
//        //check mandatory fields
//        if (name == null) {
//            throw new InvalidResourceException(InvalidResourceException.ERROR_TYPE.NO_AGENT_NAME);
//        }
//
//        if (identifier == null) {
//            throw new InvalidResourceException(InvalidResourceException.ERROR_TYPE.NO_IDENTIFIER);
//        }
    }

}
