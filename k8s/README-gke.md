# Running the Razorpay clone on GKE

Cluster: Autopilot `razorpay-cluster`, region `asia-south1`, project `learning-kubernates-509115`.
Everything runs in the `razorpay-core` namespace. Only the API gateway is public (a Google Cloud load balancer).

```
Postman / JMeter ──► api-gateway (LoadBalancer :80)
                        ├─► merchant-service   ─┐
                        ├─► payment-service ─► vault-service
                        ├─► vault-service       ├─► postgres, redis, kafka
                        └─► operations-service ─┘
config-service (reads github.com/h493/distributed-razorpay-config, profile "k8s")
prometheus ─► grafana, zipkin (internal only, reach them with port-forward)
```

## First-time setup (already done)

```bash
gcloud container clusters create-auto razorpay-cluster --region=asia-south1 --release-channel=regular
gcloud container clusters get-credentials razorpay-cluster --region=asia-south1
cp k8s/k8s-secrets.env.example k8s/k8s-secrets.env   # then fill in values (see comments in the file)
```

## Deploy / update

```bash
kubectl apply -k k8s
kubectl get pods -n razorpay-core -w          # wait until all are 1/1 Running (~4 min from scratch)
kubectl get svc api-gateway -n razorpay-core  # EXTERNAL-IP is the Postman/JMeter base URL
```

## Shipping code changes

1. Open Docker Desktop and make sure it is signed in. Jib uses Docker Desktop's session token and fails with
   `401 incorrect username or password` when the app is closed.
2. Build and push. Install `common-library` first if you changed it:
   ```bash
   (cd common-library && mvn -q install -DskipTests)
   (cd payment-service && mvn -q package -DskipTests)   # Jib pushes himanshuchhikara21/razorpay-payment-service:latest
   ```
3. Pin the new digests and roll out:
   ```bash
   k8s/pin-images.sh
   kubectl apply -k k8s
   ```
   Pinning matters: GKE pulls Docker Hub through `mirror.gcr.io`, which can keep serving an old `:latest` for a while.

## Debugging

```bash
kubectl logs -n razorpay-core deploy/payment-service --tail=100
kubectl describe pod -n razorpay-core <pod>          # Events show image pull / scheduling / OOM problems
kubectl port-forward -n razorpay-core svc/grafana 3000:3000        # http://localhost:3000
kubectl port-forward -n razorpay-core svc/prometheus 9090:9090     # http://localhost:9090
kubectl port-forward -n razorpay-core svc/zipkin 9411:9411         # http://localhost:9411
```

## Postman

Import `k8s/postman/Razorpay-GKE.postman_collection.json` and `Razorpay-GKE.postman_environment.json`,
select the "Razorpay GKE" environment, then send requests 0 → 11 in order. Each one saves what the next needs.
If the gateway IP changes, update `baseUrl` in the environment.

## Cost control (free-trial credits)

Autopilot bills for running pods (~$0.20/hr for this stack) plus the load balancer and disks.

```bash
# pause: stop all pods, keep data + IP (~$1/day left for disks + load balancer)
kubectl scale deploy,statefulset --all --replicas=0 -n razorpay-core
# resume
kubectl scale deploy,statefulset --all --replicas=1 -n razorpay-core
# remove the whole app (deletes the databases)
kubectl delete namespace razorpay-core
```
