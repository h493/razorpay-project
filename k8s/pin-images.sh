#!/bin/sh
# Re-pin every service image in kustomization.yaml to the digest currently tagged :latest on Docker Hub.
# Run after `mvn package` (Jib) pushes new images, then `kubectl apply -k k8s`.
set -e
cd "$(dirname "$0")"
python3 - <<'EOF'
import json, re, urllib.request

text = open("kustomization.yaml").read()
for name in re.findall(r"- name: (himanshuchhikara21/\S+)", text):
    url = f"https://hub.docker.com/v2/repositories/{name}/tags/latest"
    digest = json.load(urllib.request.urlopen(url))["digest"]
    text = re.sub(rf"(- name: {re.escape(name)}\n\s+digest: )\S+", rf"\g<1>{digest}", text)
    print(f"{name:55} {digest[:19]}")
open("kustomization.yaml", "w").write(text)
EOF
