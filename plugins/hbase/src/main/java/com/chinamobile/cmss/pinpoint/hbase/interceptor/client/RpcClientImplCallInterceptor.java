package com.chinamobile.cmss.pinpoint.hbase.interceptor.client;

import com.chinamobile.cmss.pinpoint.hbase.HBaseConstants;
import com.chinamobile.cmss.pinpoint.hbase.HBaseUtil;
import com.chinamobile.cmss.pinpoint.hbase.PinPointTraceInfoProtos;
import com.google.protobuf.Descriptors;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.net.InetSocketAddress;

public class RpcClientImplCallInterceptor implements AroundInterceptor{

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public RpcClientImplCallInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }


    @Override
    public void before(Object target, Object[] args) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        PinPointTraceInfoProtos.PinPointTraceInfo.Builder builder = PinPointTraceInfoProtos.PinPointTraceInfo.newBuilder();
        if(!trace.canSampled()) {
            builder.setPinpointSampled(false);
        }else{
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(HBaseConstants.HBASE_CLIENT_SERVICE_TYPE);

            String remoteAddress = HBaseUtil.getHostPort((InetSocketAddress)args[5]);
            recorder.recordDestinationId(remoteAddress);

            Descriptors.MethodDescriptor md  = (Descriptors.MethodDescriptor)args[1];
            String serviceUrl = getServiceUrl(remoteAddress, md.getService().getName(), md.getName());
            recorder.recordAttribute(HBaseConstants.RPC_URL, serviceUrl);

            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());

            builder.setPinpointSampled(true);
            builder.setPinpointTraceId(nextId.getTransactionId());
            builder.setPinpointSpanId(nextId.getSpanId());
            builder.setPinpointParentSpanId(nextId.getParentSpanId());
            builder.setPinpointFlags(nextId.getFlags());
            builder.setPinpointParentApplicationName(traceContext.getApplicationName());
            builder.setPinpointParentApplicationType(traceContext.getServerTypeCode());
            builder.setPinpointHost(remoteAddress);

        }
        PinPointTraceInfoProtos.PinPointTraceInfo info = builder.build();
        InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        currentTransaction.setAttachment(info);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = this.traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getServiceUrl(String url, String serviceName, String methodName) {
        StringBuilder sb = new StringBuilder();
        sb.append(url).append("/").append(serviceName).append("/").append(methodName);
        return sb.toString();
    }
}
