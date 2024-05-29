package org.datashare;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.java.Log;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.JSONTransactionSerializer;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.hyperledger.fabric.shim.ChaincodeException;

import java.util.Map;
import java.util.Set;



@Serializer(target = Serializer.TARGET.TRANSACTION)
@Log
public class ValidationJSONTransactionSerializer extends JSONTransactionSerializer {

    static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();


    @Override
    public Object fromBuffer(byte[] buffer, TypeSchema ts) {

        Object obj = super.fromBuffer(buffer, ts);

        log.info(String.format("Perform parameter validation on request parameters %s" , ReflectionToStringBuilder.toString(obj , ToStringStyle.JSON_STYLE)));

        Set<ConstraintViolation<Object>> constraintViolations = VALIDATOR.validate(obj);

        if (CollectionUtils.isNotEmpty(constraintViolations)) {
            Map<String , String> err = Maps.newHashMapWithExpectedSize(constraintViolations.size());
            for (ConstraintViolation<Object> cv : constraintViolations) {
                err.put(cv.getPropertyPath().toString() , cv.getMessage());
            }
            String errMsg = String.format("Parameter verification failed, error message %s" , JSON.toJSONString(err));

            log.info(errMsg);
            throw new ChaincodeException(errMsg);

        }
        return obj;
    }
}
