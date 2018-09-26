package com.chinamobile.cmss.pinpoint.hbase.interceptor.client;

import com.chinamobile.cmss.pinpoint.hbase.accessor.MemoizedSerializedSizeGetter;
import com.chinamobile.cmss.pinpoint.hbase.accessor.MemoizedSerializedSizeSetter;
import com.chinamobile.cmss.pinpoint.hbase.accessor.PinPointTraceInfoAccessor;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import org.apache.hadoop.hbase.protobuf.generated.RPCProtos;

public class RequestHeaderSizeInterceptor implements AroundInterceptor0 {
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public RequestHeaderSizeInterceptor(TraceContext traceContext, MethodDescriptor descriptor,InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }


    @Override
    public void before(Object target) {
        if(target instanceof RPCProtos.RequestHeader && target instanceof PinPointTraceInfoAccessor &&
                target instanceof MemoizedSerializedSizeGetter && target instanceof MemoizedSerializedSizeSetter){
            RPCProtos.RequestHeader header = (RPCProtos.RequestHeader)target;
            MemoizedSerializedSizeGetter sizeGetter = (MemoizedSerializedSizeGetter)target;
            MemoizedSerializedSizeSetter sizeSetter = (MemoizedSerializedSizeSetter)target;
            PinPointTraceInfoAccessor pinPointTraceInfoAccessor  = (PinPointTraceInfoAccessor)target;

            if(pinPointTraceInfoAccessor._$PINPOINT$_getPinPointTraceInfo() == null)
                return;

            if(sizeGetter._$PINPOINT$_getMemoizedSerializedSize() == -2) {
                //-2 is a flag to calculate origin size , in order to avoid recursion
                sizeSetter._$PINPOINT$_setMemoizedSerializedSize(-1);
                return;
            }

            if(sizeGetter._$PINPOINT$_getMemoizedSerializedSize() == -1){
                sizeSetter._$PINPOINT$_setMemoizedSerializedSize(-2);
                int originSize = header.getSerializedSize();
                int traceInfoSize = pinPointTraceInfoAccessor._$PINPOINT$_getPinPointTraceInfo().getSerializedSize();
                sizeSetter._$PINPOINT$_setMemoizedSerializedSize(originSize + traceInfoSize);
            }
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}
