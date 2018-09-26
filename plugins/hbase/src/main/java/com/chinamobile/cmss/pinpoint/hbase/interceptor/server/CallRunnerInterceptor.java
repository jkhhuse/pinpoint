package com.chinamobile.cmss.pinpoint.hbase.interceptor.server;

import com.chinamobile.cmss.pinpoint.hbase.HBaseConstants;
import com.chinamobile.cmss.pinpoint.hbase.PinPointTraceInfoProtos;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.UnknownFieldSet;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import org.apache.hadoop.hbase.ipc.CallRunner;
import org.apache.hadoop.hbase.protobuf.ProtobufUtil;
import org.apache.hadoop.hbase.protobuf.generated.RPCProtos;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class CallRunnerInterceptor implements AroundInterceptor0 {

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    public CallRunnerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }


    @Override
    public void before(Object target) {
        if(target instanceof org.apache.hadoop.hbase.ipc.CallRunner){
            if (logger.isDebugEnabled()) {
                logger.beforeInterceptor(target,va());
            }

            CallRunner runner = (CallRunner) target;
            try {
                Method getHeaderMethod = runner.getCall().getClass().getDeclaredMethod("getHeader");
                getHeaderMethod.setAccessible(true);
                RPCProtos.RequestHeader requestHeader = (RPCProtos.RequestHeader)getHeaderMethod.invoke(runner.getCall());
                UnknownFieldSet unknownFieldSet = requestHeader.getUnknownFields();

                if(!unknownFieldSet.hasField(104)){
                    return;
                }

                PinPointTraceInfoProtos.PinPointTraceInfo.Builder recoveryTraceBuilder = PinPointTraceInfoProtos.PinPointTraceInfo.newBuilder();
                byte[] buf = new byte[unknownFieldSet.getSerializedSize()];
                CodedOutputStream outputStream =  CodedOutputStream.newInstance(buf);
                unknownFieldSet.writeTo(outputStream);
                ProtobufUtil.mergeFrom(recoveryTraceBuilder, buf, 0, unknownFieldSet.getSerializedSize());
                PinPointTraceInfoProtos.PinPointTraceInfo reTraceInfo = recoveryTraceBuilder.build();

                if(!reTraceInfo.getPinpointSampled()) {
                    return;
                }

                String transactionId = reTraceInfo.getPinpointTraceId();
                long parentSpanId = reTraceInfo.getPinpointParentSpanId();
                long spanId = reTraceInfo.getPinpointSpanId();
                short flags = (short)reTraceInfo.getPinpointFlags();
                TraceId traceId =  this.traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
                Trace trace = this.traceContext.continueTraceObject(traceId);

                SpanRecorder recorder = trace.getSpanRecorder();
                recorder.recordServiceType(HBaseConstants.HBASE_SERVER_SERVICE_TYPE);
                recorder.recordApi(descriptor);
                //recorder.recordRpcName();
                //recorder.recordEndPoint(server.getAddress());
                recorder.recordRemoteAddress(runner.getCall().getRemoteAddress().getHostAddress());

                String parentApplicationName = reTraceInfo.getPinpointParentApplicationName();
                short parentApplicationType = (short)reTraceInfo.getPinpointParentApplicationType();
                String acceptorHost = reTraceInfo.getPinpointHost();
                recorder.recordParentApplication(parentApplicationName, parentApplicationType);
                recorder.recordAcceptorHost(acceptorHost);
                recorder.recordEndPoint(acceptorHost);

            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, va(), result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // TODO STATDISABLE this logic was added to disable statistics tracing
        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }
        // ------------------------------------------------------
        try {
            final SpanRecorder recorder = trace.getSpanRecorder();
            if(throwable != null){
                recorder.recordException(throwable);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            traceContext.removeTraceObject();
            trace.close();
        }
    }
}
