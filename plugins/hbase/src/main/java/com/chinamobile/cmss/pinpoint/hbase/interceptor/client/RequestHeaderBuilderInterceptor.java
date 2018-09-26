package com.chinamobile.cmss.pinpoint.hbase.interceptor.client;

import com.chinamobile.cmss.pinpoint.hbase.PinPointTraceInfoProtos;
import com.chinamobile.cmss.pinpoint.hbase.accessor.PinPointTraceInfoAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

public class RequestHeaderBuilderInterceptor implements AroundInterceptor0{
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public RequestHeaderBuilderInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }


    @Override
    public void before(Object target) {

    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {
        if(result instanceof PinPointTraceInfoAccessor){
            PinPointTraceInfoAccessor accessor = (PinPointTraceInfoAccessor)result;

            InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
            PinPointTraceInfoProtos.PinPointTraceInfo parentTraceInfo = (PinPointTraceInfoProtos.PinPointTraceInfo)currentTransaction.getAttachment();
            if (parentTraceInfo == null) {
                return;
            }

            accessor._$PINPOINT$_setPinPointTraceInfo(parentTraceInfo);
        }else{
            logger.error("return type expected tobe PinPointTraceInfoAccessor");
        }
    }
}
