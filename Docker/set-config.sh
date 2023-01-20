#!/usr/bin/env bash

kubectl config set-cluster  tiffany-cluster --server=https://kubernetes.default --certificate-authority=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
kubectl config set-context tiffany-context --cluster=ada-cluster
kubectl config set-credentials tiffany-user --token="$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)"
kubectl config set-context tiffany-context --user=tiffany-user
kubectl config use-context tiffany-context
