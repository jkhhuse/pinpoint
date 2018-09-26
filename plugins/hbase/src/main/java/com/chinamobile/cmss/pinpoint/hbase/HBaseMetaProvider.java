package com.chinamobile.cmss.pinpoint.hbase;

import com.navercorp.pinpoint.common.trace.TraceMetadataProvider;
import com.navercorp.pinpoint.common.trace.TraceMetadataSetupContext;

public class HBaseMetaProvider implements TraceMetadataProvider {

    @Override
    public void setup(TraceMetadataSetupContext context) {
        context.addServiceType(HBaseConstants.HBASE_CLIENT_SERVICE_TYPE);
        context.addServiceType(HBaseConstants.HBASE_CLIENT_METHOD_SERVICE_TYPE);
        context.addServiceType(HBaseConstants.HBASE_SERVER_SERVICE_TYPE);
        context.addServiceType(HBaseConstants.HBASE_SERVER_METHOD_SERVICE_TYPE);

        context.addAnnotationKey(HBaseConstants.RPC_URL);
        context.addAnnotationKey(HBaseConstants.TABLE_NAME);
    }
}

