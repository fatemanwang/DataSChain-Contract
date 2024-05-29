package org.datashare;

import lombok.Data;
import lombok.experimental.Accessors;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.ArrayList;


@DataType
@Data
@Accessors(chain = true)
/*
* Data structure of the data directory
* */
public class Entity {

    @Property
    String id;

    @Property
    String content;

    @Property
    String uploader;

    @Property
    String uptime;

    @Property
    ArrayList cooperator;

    @Property
    ArrayList uploadPermission;

    @Property
    ArrayList downloadPermission;

    //data probe
    @Property
    String CID;
}
