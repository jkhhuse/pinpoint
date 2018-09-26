package com.chinamobile.cmss.pinpoint.hbase;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

public class HBasePlugin implements ProfilerPlugin, TransformTemplateAware{
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final HBaseConfig config = new HBaseConfig(context.getConfig());
        logger.debug("[HBase] Initialized config={}", config);

        if (config.isProfile()) {
            transformTemplate.transform("org.apache.hadoop.hbase.client.HTable", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    InstrumentMethod getMethod = target.getDeclaredMethod("get","org.apache.hadoop.hbase.client.Get");
                    getMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.HTableSimpleInterceptor",va(HBaseConstants.HBASE_CLIENT_METHOD_SERVICE_TYPE));

                    InstrumentMethod putMethod = target.getDeclaredMethod("put","org.apache.hadoop.hbase.client.Put");
                    putMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.HTableSimpleInterceptor",va(HBaseConstants.HBASE_CLIENT_METHOD_SERVICE_TYPE));

                    InstrumentMethod delMethod = target.getDeclaredMethod("delete","org.apache.hadoop.hbase.client.Delete");
                    delMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.HTableSimpleInterceptor",va(HBaseConstants.HBASE_CLIENT_METHOD_SERVICE_TYPE));

                    InstrumentMethod scanMethod = target.getDeclaredMethod("getScanner","org.apache.hadoop.hbase.client.Scan");
                    scanMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.HTableSimpleInterceptor",va(HBaseConstants.HBASE_CLIENT_METHOD_SERVICE_TYPE));

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.client.ClientScanner", new TransformCallback() {
                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    InstrumentMethod scannerNextMethod = target.getDeclaredMethod("next");
                    scannerNextMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.common.SimpleInterceptor",va(HBaseConstants.HBASE_CLIENT_METHOD_SERVICE_TYPE));

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.protobuf.generated.RPCProtos$RequestHeader", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                    target.addSetter("com.chinamobile.cmss.pinpoint.hbase.accessor.MemoizedSerializedSizeSetter","memoizedSerializedSize");
                    target.addGetter("com.chinamobile.cmss.pinpoint.hbase.accessor.MemoizedSerializedSizeGetter","memoizedSerializedSize");
                    target.addField("com.chinamobile.cmss.pinpoint.hbase.accessor.PinPointTraceInfoAccessor");

                    InterceptorScope hbaseClientScope = instrumentor.getInterceptorScope(HBaseConstants.HBASE_CLIENT_SCOPE);

                    InstrumentMethod writeToMethod = target.getDeclaredMethod("writeTo","com.google.protobuf.CodedOutputStream");
                    writeToMethod.addScopedInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.RequestHeaderWriteInterceptor",hbaseClientScope,ExecutionPolicy.INTERNAL);

                    InstrumentMethod getSerializedSizeMethod = target.getDeclaredMethod("getSerializedSize");
                    getSerializedSizeMethod.addScopedInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.RequestHeaderSizeInterceptor",hbaseClientScope,ExecutionPolicy.INTERNAL);

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.protobuf.generated.RPCProtos$RequestHeader$Builder", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    InstrumentMethod buildMethod = target.getDeclaredMethod("build");
                    InterceptorScope hbaseClientScope = instrumentor.getInterceptorScope(HBaseConstants.HBASE_CLIENT_SCOPE);
                    buildMethod.addScopedInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.RequestHeaderBuilderInterceptor",hbaseClientScope, ExecutionPolicy.INTERNAL);

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.ipc.RpcClientImpl", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    InstrumentMethod callMethod = target.getDeclaredMethod("call","org.apache.hadoop.hbase.ipc.PayloadCarryingRpcController",
                            "com.google.protobuf.Descriptors$MethodDescriptor","com.google.protobuf.Message","com.google.protobuf.Message","org.apache.hadoop.hbase.security.User",
                            "java.net.InetSocketAddress","org.apache.hadoop.hbase.client.MetricsConnection$CallStats");
                    InterceptorScope hbaseClientScope = instrumentor.getInterceptorScope(HBaseConstants.HBASE_CLIENT_SCOPE);
                    callMethod.addScopedInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.RpcClientImplCallInterceptor",hbaseClientScope, ExecutionPolicy.BOUNDARY);

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.client.AsyncProcess$AsyncRequestFutureImpl$SingleServerRequestRunnable",new TransformCallback(){

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                    target.addField(AsyncContextAccessor.class.getName());

                    InstrumentMethod constructor = target.getConstructor("org.apache.hadoop.hbase.client.AsyncProcess$AsyncRequestFutureImpl","org.apache.hadoop.hbase.client.MultiAction","int","org.apache.hadoop.hbase.ServerName","java.util.Set");
                    constructor.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.SingleServerRequestConstructorInterceptor");

                    InstrumentMethod runMethod =  target.getDeclaredMethod("run");
                    runMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.client.SingleServerRequestRunInterceptor");

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.ipc.CallRunner", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    InstrumentMethod runMethod = target.getDeclaredMethod("run");
                    runMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.server.CallRunnerInterceptor");

                    return target.toBytecode();
                }
            });

            transformTemplate.transform("org.apache.hadoop.hbase.ipc.RpcServer", new TransformCallback() {

                @Override
                public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                    InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                    InstrumentMethod runMethod = target.getDeclaredMethod("call","com.google.protobuf.BlockingService","com.google.protobuf.Descriptors$MethodDescriptor"
                    ,"com.google.protobuf.Message","org.apache.hadoop.hbase.CellScanner","long","org.apache.hadoop.hbase.monitoring.MonitoredRPCHandler");
                    runMethod.addInterceptor("com.chinamobile.cmss.pinpoint.hbase.interceptor.common.SimpleInterceptor",va(HBaseConstants.HBASE_SERVER_METHOD_SERVICE_TYPE));

                    return target.toBytecode();
                }
            });
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}
