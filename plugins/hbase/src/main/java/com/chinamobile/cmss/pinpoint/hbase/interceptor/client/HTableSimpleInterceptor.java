package com.chinamobile.cmss.pinpoint.hbase.interceptor.client;

import com.chinamobile.cmss.pinpoint.hbase.HBaseConstants;
import com.chinamobile.cmss.pinpoint.hbase.interceptor.common.SimpleInterceptor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.client.HTable;

public class HTableSimpleInterceptor extends SimpleInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public HTableSimpleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, ServiceType serviceType) {
        super(traceContext,descriptor,serviceType);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }
        super.before(target,args);

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        HTable table = (HTable)target;
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        recorder.recordAttribute(HBaseConstants.TABLE_NAME,table.getName().getNameWithNamespaceInclAsString());
    }
}
