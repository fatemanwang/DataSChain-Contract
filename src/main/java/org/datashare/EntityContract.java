package org.datashare;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.java.Log;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.sql.Date;
import java.text.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;



@Contract(
        name = "EntityContract",
        info = @Info(
                title = "Entity contract",
                description = "The hyperlegendary entity contract",
                version = "0.0.1-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "f.carr@example.com",
                        name = "F Carr",
                        url = "https://hyperledger.example.com")))
@Default
@Log
public class EntityContract implements ContractInterface {


    @Transaction
    public void initLedger(final Context ctx) {

        ChaincodeStub stub = ctx.getStub();
        for (int i = 0; i < 10; i++ ) {
            Entity entity = new Entity().setId("Entity"+i)
                    .setContent("Entity"+i)
                    .setUploader("127.0.0.1")
                    .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                    .setCooperator(new ArrayList(Arrays.asList(new String[]{"user A"})))
                    .setUploadPermission(new ArrayList(Arrays.asList(new String[]{"user A"})))
                    .setDownloadPermission(new ArrayList(Arrays.asList(new String[]{"user A"})));
            stub.putStringState(entity.getContent() , JSON.toJSONString(entity));
        }

    }

    //Verify User Collaborator Permissions
    @Transaction
    public boolean varifyCooperator(final Context ctx , final String key ,final String sysUser){
        ChaincodeStub stub = ctx.getStub();
        String dataState = stub.getStringState(key);

        if(StringUtils.isBlank(dataState)){
            String errorMessage=String.format("Data %s does not exist",key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity entity=JSON.parseObject(dataState , Entity.class);
        if(!entity.getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s no cooperation permission",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        return true;
    }

    //Update the collaborator permissions of a data directory
    @Transaction
    public Entity updateCooperator(final Context ctx, final String key , final String content , final String uploader, String cooperator, String downloadPermission,final String sysUser, final String CID ) {
        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity preEntity = JSON.parseObject(entityState , Entity.class);
        if(!preEntity.getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s no permission to modify collaborators collection",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        Entity entity = new Entity().setId(key)
                .setContent(content)
                .setUploader(uploader)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                .setUploadPermission(null)
                .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                .setCID(CID);

        stub.putStringState(key, JSON.toJSONString(entity));

        return entity;
    }

    //Verify User Downloader Permissions
    @Transaction
    public boolean varifyDownload(final Context ctx , final String key ,final String sysUser){
        ChaincodeStub stub = ctx.getStub();
        String dataState = stub.getStringState(key);

        if(StringUtils.isBlank(dataState)){
            String errorMessage=String.format("Data %s does not exist",key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity entity=JSON.parseObject(dataState , Entity.class);
        if(!entity.getDownloadPermission().contains(sysUser)){
            String errorMessage=String.format("%s no download permission",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        return true;
    }

    //Update the downloader permission of a data directory
    @Transaction
    public Entity updateDownload(final Context ctx, final String key , final String content , final String uploader, String cooperator, String downloadPermission,final String sysUser ) {
        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity preEntity = JSON.parseObject(entityState , Entity.class);
        if(!preEntity.getDownloadPermission().contains(sysUser)){
            String errorMessage=String.format("%s No permission to modify downloaders collection",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        Entity entity = new Entity().setId(key)
                .setContent(content)
                .setUploader(uploader)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                .setUploadPermission(null)
                .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))));

        stub.putStringState(key, JSON.toJSONString(entity));

        return entity;
    }

    //Verify User Uploader Permissions
    @Transaction
    public boolean varifyUpload(final Context ctx ,final String sysUser){
        ChaincodeStub stub = ctx.getStub();
        String dataState = stub.getStringState("entity-0");

        if(StringUtils.isBlank(dataState)){
            String errorMessage=String.format("Data %s does not exist","entity-0");
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity entity=JSON.parseObject(dataState , Entity.class);
        if(!entity.getUploadPermission().contains(sysUser)){
            String errorMessage=String.format("%s No upload permission",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        return true;
    }

    //Update the uploader permissions of the data directory
    @Transaction
    public Entity updateUpload(final Context ctx, String uploadPermission,final String sysUser ) {
        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState("entity-0");

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", "entity-0");
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity preEntity = JSON.parseObject(entityState , Entity.class);
        if(!preEntity.getUploadPermission().contains(sysUser)){
            String errorMessage=String.format("%s No permission to modify uploaders collection",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        Entity entity = new Entity().setId("entity-0")
                .setContent(null)
                .setUploader(null)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(null)
                .setUploadPermission(new ArrayList(Arrays.asList(uploadPermission.split(","))))
                .setDownloadPermission(null)
                .setCID(null);

        stub.putStringState("entity-0", JSON.toJSONString(entity));

        return entity;
    }

    //Query a data directory by key
    @Transaction
    public Entity queryEntity(final Context ctx, final String key) {

        ChaincodeStub stub = ctx.getStub();
        String dataState = stub.getStringState(key);

        if (StringUtils.isBlank(dataState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(dataState , Entity.class);
    }

    //Query all public data directories (dictionary type: entities: "")
    @Transaction
    public EntityQueryResultList queryAllEntities(final Context ctx) {

        EntityQueryResultList resultList = new EntityQueryResultList();
        QueryResultsIterator<KeyValue> queryResult = ctx.getStub().getStateByRange("","");
        List<EntityQueryResult> results = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(new EntityQueryResult().setKey(kv.getKey()).setEntity(JSON.parseObject(kv.getStringValue() , Entity.class)));
            }
            resultList.setEntities(results);
        }

        return resultList;
    }

    //Query all public data directories (dictionary type: entityList: "")
    @Transaction
    public EntityList queryAllEntities_2(final Context ctx) {

        EntityList resultList=new EntityList();
        List<Entity> results=Lists.newArrayList();
        QueryResultsIterator<KeyValue> queryResult = ctx.getStub().getStateByRange("","");

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(JSON.parseObject(kv.getStringValue(),Entity.class));
            }

            resultList.setEntityList(results);
        }

        return resultList;
    }

    /*
    * Query data according to the uploader
    * return EntityList
    * */
    @Transaction
    public EntityList queryEntityByUploader(final Context ctx, String uploader) {

        log.info(String.format("Query data according to the data uploader, uploader = %s" , uploader));

        String query = String.format("{\"selector\":{\"uploader\":\"%s\"} , \"use_index\":[\"_design/indexUploaderUptimeDoc\", \"indexUploaderUptime\"]}", uploader);

        log.info(String.format("query string = %s" , query));
        return queryEntity(ctx.getStub() , query);
    }

    /*
     * Query data according to the uploader
     * return EntityQueryPageResult
     * */
    @Transaction
    public EntityQueryPageResult queryEntityPageByUploader(final Context ctx, String uploader , Integer pageSize , String bookmark) {

        log.info(String.format("Query data by pagination according to the data uploader , uploader = %s" , uploader));

        String query = String.format("{\"selector\":{\"uploader\":\"%s\"} , \"use_index\":[\"_design/indexUploaderUptimeDoc\", \"indexUploaderUptime\"]}", uploader);

        log.info(String.format("query string = %s" , query));

        ChaincodeStub stub = ctx.getStub();
        QueryResultsIteratorWithMetadata<KeyValue> queryResult = stub.getQueryResultWithPagination(query, pageSize, bookmark);

        List<EntityQueryResult> entities = Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                entities.add(new EntityQueryResult().setKey(kv.getKey()).setEntity(JSON.parseObject(kv.getStringValue() , Entity.class)));
            }
        }

        return new EntityQueryPageResult()
                .setEntities(entities)
                .setBookmark(queryResult.getMetadata().getBookmark());
    }

    /*
     * Query data according to the uploader and upload time
     * return EntityList
     * */
    @Transaction
    public EntityList queryEntityByUploaderAndUptime(final Context ctx, String uploader , String uptime) {

        log.info(String.format("use uploader & uptime query data , uploader = %s , uptime = %s" , uploader , uptime));

        String query = String.format("{\"selector\":{\"uploader\":\"%s\" , \"uptime\":\"%s\"} , \"use_index\":[\"_design/indexUploaderUptimeDoc\", \"indexUploaderUptime\"]}", uploader , uptime);
        return queryEntity(ctx.getStub() , query);
    }

    //Add the first piece of data directory to store the permissions of the uploader
    @Transaction
    public Entity createUploader(final Context ctx, String uploadPermission) {

        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState("entity-0");

        if (StringUtils.isNotBlank(entityState)) {
            String errorMessage = String.format("Data %s already exists", "entity-0");
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }


        Entity entity = new Entity().setId("entity-0")
                .setContent(null)
                .setUploader(null)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(null)
                .setUploadPermission(new ArrayList(Arrays.asList(uploadPermission.split(","))))
                .setDownloadPermission(null)
                .setCID(null);

        String json = JSON.toJSONString(entity);
        stub.putStringState("entity-0", json);

        stub.setEvent("createUploaderEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));

        return entity;
    }

    /*
     * Query data based on conditional statements
     * return EntityList
     * */
    private EntityList queryEntity(ChaincodeStub stub , String query) {

        EntityList resultList=new EntityList();
        QueryResultsIterator<KeyValue> queryResult = stub.getQueryResult(query);
        List<Entity> results= Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(JSON.parseObject(kv.getStringValue(),Entity.class));
            }
            resultList.setEntityList(results);
        }

        return resultList;
    }

   //Added data directory information (including access rights to the data directory)
    @Transaction
    public Entity createEntity(final Context ctx, final String key , String content ,  String uploader , final String cooperator, final String downloadPermission, final String sysUser, final String CID) {

        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isNotBlank(entityState)) {
            String errorMessage = String.format("Data %s already exists", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        String uploadState=stub.getStringState("entity-0");
        Entity uploadEntity=JSON.parseObject(uploadState , Entity.class);
        if(!uploadEntity.getUploadPermission().contains(sysUser)){
            String errorMessage = String.format(" %s no data directory upload permission" , sysUser);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity entity = new Entity().setId(key)
                .setContent(content)
                .setUploader(uploader)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                .setUploadPermission(null)
                .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                .setCID(CID);

        String json = JSON.toJSONString(entity);
        stub.putStringState(key, json);

        stub.setEvent("createEntityEvent" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));
        return entity;
    }

    //update data directory
    @Transaction
    public Entity updateEntity(final Context ctx, final String key , String content ,  String uploader,final String cooperator,final String downloadPermission,final String sysUser ,String CID) {

        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity preEntity = JSON.parseObject(entityState , Entity.class);
        if(!preEntity.getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s no permission to modify the data directory",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        Entity entity = new Entity().setId(key)
                .setContent(content)
                .setUploader(uploader)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                .setUploadPermission(null)
                .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                .setCID(CID);

        stub.putStringState(key, JSON.toJSONString(entity));

        return entity;
    }

    //delete data directory
    @Transaction
    public Entity deleteEntity(final Context ctx, final String key , final String sysUser) {

        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        Entity preEntity = JSON.parseObject(entityState , Entity.class);
        if(!preEntity.getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s No permission to delete the data directory",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        stub.delState(key);

        return JSON.parseObject(entityState , Entity.class);
    }

    @Transaction
    public byte[] queryPrivateEntityHash(final Context ctx, final String collection ,final String key) {

        ChaincodeStub stub = ctx.getStub();

        byte[] hash = stub.getPrivateDataHash(collection, key);

        if (ArrayUtils.isEmpty(hash)) {
            String errorMessage = String.format("Private Data %s does not exist", key);
            log.log(Level.WARNING , errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return hash;
    }

    /*
    * Query private data directory according to the key
    * return PrivateEntity
    * */
    @Transaction
    public PrivateEntity queryPrivateEntity(final Context ctx, final String collection , final String key) {

        ChaincodeStub stub = ctx.getStub();

        log.info(String.format("Query private data directory , collection [%s] key [%s] , mspId [%s] " , collection , stub.getMspId() , key));

        String entityState = stub.getPrivateDataUTF8(collection , key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Private Data %s does not exist", key);
            log.log(Level.WARNING , errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        return JSON.parseObject(entityState , PrivateEntity.class);
    }

    /*
     * Query private data directory based on conditional statements
     * return PrivateEntityList
     * */
    private PrivateEntityList queryPrivateEntity(ChaincodeStub stub , String collection,String query) {

        PrivateEntityList resultList=new PrivateEntityList();
        QueryResultsIterator<KeyValue> queryResult = stub.getPrivateDataQueryResult(collection,query);
        List<PrivateEntity> results= Lists.newArrayList();

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(JSON.parseObject(kv.getStringValue(),PrivateEntity.class));
            }
            resultList.setPrivateEntityList(results);
        }

        return resultList;
    }

    /*
     * Query private data directory based on uploader
     * return PrivateEntityList
     * */
    @Transaction
    public PrivateEntityList queryPrivateEntityByUploader(final Context ctx, String collection,String uploader) {

        log.info(String.format("Query data according to data uploader, uploader = %s" , uploader));

        String query = String.format("{\"selector\":{\"uploader\":\"%s\"} , \"use_index\":[\"_design/indexUploaderUptimeDoc\", \"indexUploaderUptime\"]}", uploader);

        log.info(String.format("query string = %s" , query));
        return queryPrivateEntity(ctx.getStub() ,collection, query);
    }

    //Query all private data directories (dictionary type: PrivateEntityList: "")
    @Transaction
    public PrivateEntityList queryAllPrivateEntities(final Context ctx,String collection) {

        PrivateEntityList resultList=new PrivateEntityList();
        List<PrivateEntity> results=Lists.newArrayList();
        QueryResultsIterator<KeyValue> queryResult = ctx.getStub().getPrivateDataByRange(collection,"","");

        if (! IterableUtils.isEmpty(queryResult)) {
            for (KeyValue kv : queryResult) {
                results.add(JSON.parseObject(kv.getStringValue(),PrivateEntity.class));
            }

            resultList.setPrivateEntityList(results);
        }

        return resultList;
    }

    //Add the first piece of private data directory to store the permissions of the uploader
    @Transaction
    public Entity createPrivateUploader(final Context ctx,final String collection, String uploadPermission) {

        ChaincodeStub stub = ctx.getStub();
        log.info(String.format("Create private data , collection [%s] , mspId [%s] , key [%s] " , collection , stub.getMspId() , "entity-0"));

        String entityState = stub.getStringState("entity-0");

        if (StringUtils.isNotBlank(entityState)) {
            String errorMessage = String.format("group mspId [%s] 的PrivateData %s already exists", stub.getMspId(),"entity-0");
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }


        Entity entity = new Entity().setId("entity-0")
                .setContent(null)
                .setUploader(null)
                .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                .setCooperator(null)
                .setUploadPermission(new ArrayList(Arrays.asList(uploadPermission.split(","))))
                .setDownloadPermission(null)
                .setCID(null);

        String json = JSON.toJSONString(entity);
        stub.putStringState("entity-0", json);

        stub.setEvent("createPrivateUploader" , org.apache.commons.codec.binary.StringUtils.getBytesUtf8(json));

        return entity;
    }


    //Add private data directory
    @Transaction
    public PrivateEntity createPrivateEntity(final Context ctx, final String collection , final String key , String content , String uploader , final String cooperator, final String downloadPermission, final String sysUser, final String CID ) {

        ChaincodeStub stub = ctx.getStub();
        log.info(String.format("Add private data , collection [%s] , mspId [%s] , key [%s] , content[%s] uploader[%s] " , collection , stub.getMspId() , key , content,uploader));

        String entityState = stub.getPrivateDataUTF8(collection , key);

        if (StringUtils.isNotBlank(entityState)) {
            String errorMessage = String.format("Private Data %s already exists", key);
            log.log(Level.WARNING , errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        String uploadState=stub.getPrivateDataUTF8(collection , "entity-0");
        PrivateEntity uploadEntity=JSON.parseObject(uploadState , PrivateEntity.class);
        if(!uploadEntity.getEntity().getUploadPermission().contains(sysUser)){
            String errorMessage = String.format(" %s No data directory upload permission" , sysUser);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        PrivateEntity entity = new PrivateEntity()
                .setEntity(new Entity().setId(key)
                        .setContent(content)
                        .setUploader(uploader)
                        .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                        .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                        .setUploadPermission(null)
                        .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                        .setCID(CID))
                        .setCollection(collection);

        String json = JSON.toJSONString(entity);

        log.info(String.format("data to save %s" , json));

        stub.putPrivateData(collection , key , json);

        return entity;
    }

    //Update the collaborator permission of a private data directory
    @Transaction
    public PrivateEntity updatePrivateCooperator(final Context ctx, final String collection, final String key , final String content , final String uploader, String cooperator, String downloadPermission,final String sysUser, final String CID ) {
        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        PrivateEntity preEntity = JSON.parseObject(entityState , PrivateEntity.class);
        if(!preEntity.getEntity().getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s No permission to modify collaborators collection",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        PrivateEntity entity = new PrivateEntity()
                .setEntity(new Entity().setId(key)
                        .setContent(content)
                        .setUploader(uploader)
                        .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                        .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                        .setUploadPermission(null)
                        .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                        .setCID(CID))
                .setCollection(collection);

        String json = JSON.toJSONString(entity);

        log.info(String.format("Data after modifying collaborator permissions %s" , json));

        stub.putPrivateData(collection , key , json);

        return entity;
    }

    //Update the downloader permission of a private data directory
    @Transaction
    public PrivateEntity updatePrivateDownload(final Context ctx, final String collection, final String key , final String content , final String uploader, String cooperator, String downloadPermission,final String sysUser, final String CID ) {
        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState(key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", key);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        PrivateEntity preEntity = JSON.parseObject(entityState , PrivateEntity.class);
        if(!preEntity.getEntity().getDownloadPermission().contains(sysUser)){
            String errorMessage=String.format("%s No permission to modify downloader collection",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        PrivateEntity entity = new PrivateEntity()
                .setEntity(new Entity().setId(key)
                        .setContent(content)
                        .setUploader(uploader)
                        .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                        .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                        .setUploadPermission(null)
                        .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                        .setCID(CID))
                .setCollection(collection);

        String json = JSON.toJSONString(entity);

        log.info(String.format("Data after modifying downloader permissions %s" , json));

        stub.putPrivateData(collection , key , json);

        return entity;
    }

    //Update uploader permissions for private data directory
    @Transaction
    public PrivateEntity updatePrivateUpload(final Context ctx, final String collection, String uploadPermission,final String sysUser ) {
        ChaincodeStub stub = ctx.getStub();
        String entityState = stub.getStringState("entity-0");

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Data %s does not exist", "entity-0");
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        PrivateEntity preEntity = JSON.parseObject(entityState , PrivateEntity.class);
        if(!preEntity.getEntity().getUploadPermission().contains(sysUser)){
            String errorMessage=String.format("%s No permission to modify uploader collection",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        PrivateEntity entity = new PrivateEntity()
                .setEntity(new Entity().setId("entity-0")
                        .setContent(null)
                        .setUploader(null)
                        .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                        .setCooperator(null)
                        .setUploadPermission(new ArrayList(Arrays.asList(uploadPermission.split(","))))
                        .setDownloadPermission(null)
                        .setCID(null))
                .setCollection(collection);

        String json = JSON.toJSONString(entity);

        log.info(String.format("Data after modifying uploader collection permissions %s" , json));

        stub.putPrivateData(collection , "entity-0", json);

        return entity;
    }

    //Update private data directory information
    @Transaction
    public PrivateEntity updatePrivateEntity(final Context ctx, final String collection, final String key , String content , String uploader,final String cooperator,final String downloadPermission,final String sysUser ,String CID ) {

        ChaincodeStub stub = ctx.getStub();
        log.info(String.format("Update private data , collection [%s] , mspId [%s] , key [%s] , content[%s] uploader[%s] " , collection , stub.getMspId() , key , content,uploader));

        String entityState = stub.getPrivateDataUTF8(collection , key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Private Data %s does not exist", key);
            log.log(Level.WARNING , errorMessage);
            throw new ChaincodeException(errorMessage);
        }

        PrivateEntity preEntity = JSON.parseObject(entityState , PrivateEntity.class);
        if(!preEntity.getEntity().getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s No permission to modify the data directory",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        PrivateEntity entity = new PrivateEntity()
                .setEntity(new Entity().setId(key)
                        .setContent(content)
                        .setUploader(uploader)
                        .setUptime(DateFormat.getDateInstance().format(new java.util.Date()))
                        .setCooperator(new ArrayList(Arrays.asList(cooperator.split(","))))
                        .setUploadPermission(null)
                        .setDownloadPermission(new ArrayList(Arrays.asList(downloadPermission.split(","))))
                        .setCID(CID))
                .setCollection(collection);

        String json = JSON.toJSONString(entity);

        log.info(String.format("modified data %s" , json));

        stub.putPrivateData(collection , key , json);

        return entity;
    }

    //Delete a private data directory
    @Transaction
    public PrivateEntity deletePrivateEntity(final Context ctx, final String collection , final String key,final String sysUser) {

        ChaincodeStub stub = ctx.getStub();

        log.info(String.format("Delete private data , collection [%s] , mspId [%s] , key [%s] " , collection , stub.getMspId() , key));

        String entityState = stub.getPrivateDataUTF8(collection , key);

        if (StringUtils.isBlank(entityState)) {
            String errorMessage = String.format("Private Data %s does not exist", key);
            log.log(Level.WARNING , errorMessage);

            throw new ChaincodeException(errorMessage);
        }

        PrivateEntity preEntity = JSON.parseObject(entityState , PrivateEntity.class);
        if(!preEntity.getEntity().getCooperator().contains(sysUser)){
            String errorMessage=String.format("%s No permission to delete the data directory",sysUser);
            System.out.println(errorMessage);
            throw  new ChaincodeException(errorMessage);
        }

        stub.delPrivateData(collection , key);

        return JSON.parseObject(entityState , PrivateEntity.class);
    }

    @Override
    public void beforeTransaction(Context ctx) {
        log.info("*************************************** beforeTransaction ***************************************");
    }

    @Override
    public void afterTransaction(Context ctx, Object result) {
        log.info("*************************************** afterTransaction ***************************************");
        System.out.println("result --------> " + result);
    }
}
