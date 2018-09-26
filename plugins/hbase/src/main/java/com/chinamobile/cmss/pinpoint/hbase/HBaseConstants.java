package com.chinamobile.cmss.pinpoint.hbase;

import com.navercorp.pinpoint.common.trace.*;

import static com.navercorp.pinpoint.common.trace.AnnotationKeyProperty.VIEW_IN_RECORD_SET;
import static com.navercorp.pinpoint.common.trace.ServiceTypeProperty.RECORD_STATISTICS;

public class HBaseConstants {
    private HBaseConstants(){};

    public static final ServiceType HBASE_CLIENT_SERVICE_TYPE = ServiceTypeFactory.of(9901, "HBASE_CLIENT", RECORD_STATISTICS);
    public static final ServiceType HBASE_CLIENT_METHOD_SERVICE_TYPE = ServiceTypeFactory.of(9902, "HBASE_CLIENT_METHOD");
    public static final ServiceType HBASE_SERVER_SERVICE_TYPE = ServiceTypeFactory.of(1900, "HBASE_SERVER", RECORD_STATISTICS);
    public static final ServiceType HBASE_SERVER_METHOD_SERVICE_TYPE = ServiceTypeFactory.of(1901, "HBASE_SERVER_METHOD");
    public static final String HBASE_CLIENT_SCOPE = "HbaseClientScope";

    public static final String UNKNOWN_ADDRESS = "Unknown";

    public static final AnnotationKey RPC_URL = AnnotationKeyFactory.of(900, "hbase.client.rpc.url", VIEW_IN_RECORD_SET);
    public static final AnnotationKey TABLE_NAME = AnnotationKeyFactory.of(901, "hbase.htable.name", VIEW_IN_RECORD_SET);

}
