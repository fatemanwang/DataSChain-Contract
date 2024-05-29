package org.datashare;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.List;

@Data
@DataType
@Accessors(chain = true)
public class PrivateEntityList {

    @Property
    List<PrivateEntity> privateEntityList;
}
