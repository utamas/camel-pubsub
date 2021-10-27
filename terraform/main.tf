resource "google_pubsub_topic" "phoenix_task_request" {
  name = "phoenix-task-request"
  depends_on = [module.pubsub]
}

resource "google_pubsub_subscription" "blah" {
  name  = "phoenix-task-response"
  topic = google_pubsub_topic.phoenix_task_request.id

  depends_on = [google_pubsub_topic.phoenix_task_request]
}

module "pubsub" {
  source = "git@github.com:patientsknowbest/terraform-modules.git//google-pubsub-emulator/?ref=pubsub-emulator-1.0.0"

  external_port = "32085"
  namespace = kubernetes_namespace.default.metadata[0].name

  termination_grace_period_seconds = 0

  depends_on = [kubernetes_namespace.default]
}

resource "kubernetes_namespace" "default" {
  metadata {
    name = "reactive"
  }
}

provider "kubernetes" {
  config_path    = "~/.kube/dev-local"
  config_context = "microk8s"
}

provider "google" {
  pubsub_custom_endpoint = "http://localhost:${module.pubsub.external_port}/v1/"
  project                = module.pubsub.project
  access_token           = "dummy_token"
}

terraform {
  required_version = ">=1.0.0, <2.0.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "3.89.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "2.6.1"
    }
    template = {
      source  = "hashicorp/template"
      version = "2.2.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "3.1.0"
    }
  }

  backend "local" {
    path = "./.state/terraform"
  }
}
