FROM amazoncorretto:17

RUN yum install wget tar gzip unzip -y

# Install dapr CLI
RUN wget -q https://raw.githubusercontent.com/dapr/cli/master/install/install.sh -O - | /bin/bash

# Install daprd
ARG DAPR_BUILD_DIR
COPY $DAPR_BUILD_DIR /opt/dapr
ENV PATH="/opt/dapr/:${PATH}"

# initialize dapr in slim mode (not containerized as we already are inside of a container)
RUN dapr init --slim

WORKDIR /opt/dapr

# build and run app
CMD /bin/bash ./dapr-run.sh