package org.datashare;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;



@DataType
@Data
public class UserInfo {

    @NotBlank(message = "key cannot be empty")
    @Property
    String key;

    @NotBlank(message = "idCard cannot be empty")
    @Property
    String idCard;

    @NotBlank(message = "name cannot be empty")
    @Length(max = 30 , message = "name cannot exceed 30 characters")
    @Property
    String name;

    @NotBlank(message = "sex cannot be empty")
    @Property
    String sex;

    @NotBlank(message = "birthday cannot be empty")
    @Property
    String birthday;

    @Property
    String phone;


}
