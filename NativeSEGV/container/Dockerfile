FROM openjdk:17-alpine AS builder
ENV JAVA_HOME=/opt/openjdk-17
RUN apk add cmake git gcc make musl-dev
RUN git clone --depth 1 https://github.com/YaSuenag/garakuta.git
WORKDIR /garakuta/NativeSEGV/build
RUN cmake .. && make

FROM openjdk:17-alpine
COPY --from=builder /garakuta/NativeSEGV/build/NativeSEGV.jar \
                    /garakuta/NativeSEGV/build/libsegv.so /opt/
CMD ["/opt/openjdk-17/bin/java", "-XX:+ErrorFileToStderr", \
     "-jar", "/opt/NativeSEGV.jar"]
