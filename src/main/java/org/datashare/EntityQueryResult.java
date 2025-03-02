package org.datashare;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;



@DataType
@Data
@Accessors(chain = true)
public class EntityQueryResult {

    @Property
    String key;

    @Property
    Entity entity;


}
