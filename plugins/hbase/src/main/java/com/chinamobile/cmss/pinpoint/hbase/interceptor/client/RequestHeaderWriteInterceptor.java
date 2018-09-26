package com.chinamobile.cmss.pinpoint.hbase.interceptor.client;

import com.chinamobile.cmss.pinpoint.hbase.PinPointTraceInfoProtos;
import com.chinamobile.cmss.pinpoint.hbase.accessor.PinPointTraceInfoAccessor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.io.IOException;

public class RequestHeaderWriteInterceptor implements AroundInterceptor1 {
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public RequestHeaderWriteInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object arg0) {

    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        if(arg0 instanceof com.google.protobuf.CodedOutputStream && target instanceof PinPointTraceInfoAccessor){
            com.google.protobuf.CodedOutputStream output  = (com.google.protobuf.CodedOutputStream)arg0;
            PinPointTraceInfoAccessor traceInfoAccessor = (PinPointTraceInfoAccessor)target;
            PinPointTraceInfoProtos.PinPointTraceInfo info  = traceInfoAccessor._$PINPOINT$_getPinPointTraceInfo();
            if(info != null) {
                try {
                    info.writeTo(output);
                } catch (IOException e) {
                    new RuntimeException(e);
                }
            }
        }
    }
}
