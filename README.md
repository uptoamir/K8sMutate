# K8sMutate

K8sMutate is a flexible Kubernetes Mutating Admission Webhook built with Java and Spring Boot. It allows users to define custom mutation rules for Kubernetes resources, such as Pods, by applying specific patches based on configurable rules.

## Features

- **Customizable Mutations**: Define your own mutation rules in a YAML configuration file.
- **Multi-Container Support**: Target specific containers within multi-container Pods.
- **Easy Integration**: Integrates seamlessly into your Kubernetes cluster with minimal setup.
- **Logging and Error Handling**: Provides detailed logging and error reporting to facilitate debugging.

## Getting Started

### Prerequisites

- Java 21 or later
- Docker (for containerization)
- Kubernetes cluster
- Maven (for building the project)

### Setup

#### 1. Clone the Repository

```bash
git clone https://github.com/your-username/k8smutate.git
cd k8smutate
```

#### 2. Build the Project

```bash
mvn clean package
```

#### 3. Run the Webhook Locally

You can run the webhook locally for testing purposes:

```bash
java -jar target/k8smutate-1.0-SNAPSHOT.jar
```

### Docker Setup

#### 1. Build the Docker Image

```bash
docker build -t your-docker-repo/k8smutate:latest .
```

#### 2. Run the Docker Container

```bash
docker run -d -p 8080:8080 \
  -v /path/to/your/application.yml:/app/config/application.yml \
  your-docker-repo/k8smutate:latest
```

## Kubernetes Integration

### 1. Deploy the Webhook to Kubernetes

Ensure that your webhook is accessible to your Kubernetes API server. You can deploy it as a service within your Kubernetes cluster.

Example deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: k8smutate
spec:
  replicas: 1
  selector:
    matchLabels:
      app: k8smutate
  template:
    metadata:
      labels:
        app: k8smutate
    spec:
      containers:
      - name: k8smutate
        image: your-docker-repo/k8smutate:latest
        ports:
        - containerPort: 8080
        volumeMounts:
        - name: config-volume
          mountPath: /app/config/application.yml
          subPath: application.yml
      volumes:
      - name: config-volume
        configMap:
          name: k8smutate-config
```

### 2. Create the MutatingWebhookConfiguration

Create a MutatingWebhookConfiguration resource to tell Kubernetes to send Pod creation requests to your webhook for mutation.

Example configuration:

```yaml
apiVersion: admissionregistration.k8s.io/v1
kind: MutatingWebhookConfiguration
metadata:
  name: k8smutate-webhook
webhooks:
  - name: mutate-pods.k8smutate.com
    clientConfig:
      service:
        name: k8smutate
        namespace: default
        path: /mutate
      caBundle: <Base64-encoded-CA-cert>
    rules:
      - operations: ["CREATE"]
        apiGroups: [""]
        apiVersions: ["v1"]
        resources: ["pods"]
    admissionReviewVersions: ["v1"]
    sideEffects: None
```

## Configuration

The K8sMutate webhook is configured using a YAML file (application.yml) that defines the mutation rules.

Example application.yml:

```yaml
mutations:
  - kind: Pod
    path: "/spec/containers/0/resources/requests/cpu"
    value: "500m"
  - kind: Pod
    path: "/spec/containers/1/resources/requests/memory"
    value: "256Mi"
```

### Understanding the Mutation Path

In the configuration file (application.yml), the mutation paths like `/spec/containers/0/resources/requests/cpu` are used to specify which part of the Kubernetes resource you want to mutate. Here's a breakdown of what this path means:

- `/spec/containers`: This refers to the array of containers defined in the Pod's specification.
- `/spec/containers/0`: The `0` here is an index that refers to the first container in the list. In programming, arrays are typically zero-indexed, meaning the first element is at index 0, the second at 1, and so on.
- `/spec/containers/0/resources/requests/cpu`: This specifically targets the CPU resource request of the first container in the Pod.

If your Pod has multiple containers and you want to target a different container, you would change the index from 0 to the appropriate number that corresponds to the desired container:

- `/spec/containers/1/resources/requests/cpu`: This would refer to the CPU resource request of the second container.

Understanding this structure is key to effectively using K8sMutate to apply mutations to specific containers within a multi-container Pod.

## Example Usage

### Multi-Container Pod Example

An example is provided in the `examples/multi-container-pod` directory, demonstrating how to use the webhook to mutate a Pod with multiple containers.

1. Apply the Pod Definition

```bash
kubectl apply -f examples/multi-container-pod/pod.yaml
```

2. Check the Mutated Pod

After the webhook applies the mutation, inspect the Pod to see the updated resource requests:

```bash
kubectl get pod multi-container-pod -o yaml
```