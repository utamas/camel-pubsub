resource "kubernetes_deployment" "example" {
  metadata {
    name      = "terraform-example"
    namespace = kubernetes_namespace.java_pubsub.metadata[0].name
    labels = {
      test = "java-pubsub-example"
    }
  }

  spec {
    replicas = 1

    selector {
      match_labels = {
        test = "java-pubsub-example"
      }
    }

    template {
      metadata {
        labels = {
          test = "java-pubsub-example"
        }
      }

      spec {
        service_account_name = kubernetes_service_account.java_pubsub.metadata[0].name

        container {
//          image = "eu.gcr.io/infra-240614/java-pubsub@sha256:63a090798fec356854ee2fc8607689d48385c360c2f6f94dd514d7c845d97589"
//          image = "eu.gcr.io/infra-240614/java-pubsub@sha256:9496fc84808965b195a35ec1a861fdb395756bed2b619b1b22dde7a15b8b4df4"
          image = "eu.gcr.io/infra-240614/java-pubsub@sha256:053e201f9b1d3919411c2825e2e1a9e77526a96a10209d8adcb0829ee070a09f"
          name = "java-pubsub"

          env {
            name = "EXTRA_JAVA_OPTS"
            value = "-Droot.log.level=TRACE"
          }
        }
      }
    }
  }
}

resource "google_service_account_iam_binding" "workload_identity_users" {
  service_account_id = data.google_service_account.kms.id
  role               = "roles/iam.workloadIdentityUser"

  members = [
    "serviceAccount:${data.google_project.current.project_id}.svc.id.goog[${kubernetes_namespace.java_pubsub.metadata[0].name}/${kubernetes_service_account.java_pubsub.metadata[0].name}]",
  ]
}

data "google_service_account" "kms" {
  account_id = "kms-05"
}

resource "kubernetes_service_account" "java_pubsub" {
  metadata {
    name      = "java-pubsub"
    namespace = kubernetes_namespace.java_pubsub.metadata[0].name
    annotations = {
      "iam.gke.io/gcp-service-account" = data.google_service_account.kms.email
    }
  }
}

resource "kubernetes_namespace" "java_pubsub" {
  metadata {
    name = "jps"
  }
}

provider "kubernetes" {
  host                   = "https://${data.google_container_cluster.current.endpoint}"
  token                  = data.google_client_config.current.access_token
  cluster_ca_certificate = base64decode(data.google_container_cluster.current.master_auth[0].cluster_ca_certificate)
}

data "google_client_config" "current" {
}

data "google_container_cluster" "current" {
  name     = "test"
  location = "europe-west2-a"
}

provider "google" {
  project = "fhir-experiments-20210712"
}

data "google_project" "current" {
}

terraform {
  required_version = ">=1.0.0, <2.0.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "3.80.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "2.4.1"
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
