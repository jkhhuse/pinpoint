package com.chinamobile.cmss.pinpoint.hbase.interceptor.client;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * used for async thread tracing
 */
public class SingleServerRequestConstructorInterceptor implements AroundInterceptor{
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public SingleServerRequestConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if(trace == null){
            return;
        }

        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        AsyncContext asyncContext = recorder.recordNextAsyncContext();

        if(target instanceof AsyncContextAccessor){
            AsyncContextAccessor asyncContextAccessor = (AsyncContextAccessor)target;
            asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
        }else{
            logger.error("target expected to be AsyncContextAccessor");
        }
    }
}
