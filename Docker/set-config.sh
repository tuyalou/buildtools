#!/usr/bin/env bash

kubectl config set-cluster  ada-cluster --server=https://kubernetes.default --certificate-authority=/var/run/secrets/kubernetes.io/serviceaccount/ca.crt
kubectl config set-context ada-context --cluster=ada-cluster
kubectl config set-credentials ada-user --token="$(cat /var/run/secrets/kubernetes.io/serviceaccount/token)"
kubectl config set-context ada-context --user=ada-user
kubectl config use-context ada-context
