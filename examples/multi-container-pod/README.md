# Multi-Container Pod Example

This example demonstrates how to use the **K8sMutate** webhook to mutate the CPU requests of specific containers within a multi-container Kubernetes Pod.

## Example Pod Definition

The `pod.yaml` file defines a Kubernetes Pod with two containers:

- **nginx-container**: An NGINX web server.
- **busybox-container**: A BusyBox utility container.

## Mutation Configuration

The `application.yml` file configures the **K8sMutate** webhook to modify the CPU requests of the containers within the Pod.

- **nginx-container**: The CPU request is set to `500m`.
- **busybox-container**: The CPU request is set to `200m`.

## How to Use

### 1. Apply the Pod Definition:

```bash
kubectl apply -f examples/multi-container-pod/pod.yaml
````
Inspect the pod
```bash
kubectl get pod multi-container-pod -o yaml
````