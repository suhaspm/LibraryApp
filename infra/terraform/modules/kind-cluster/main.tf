terraform {
  required_providers {
    kind = {
      source = "tehcyx/kind"
    }
  }
}

resource "kind_cluster" "this" {
  name           = var.name
  node_image     = var.node_image
  wait_for_ready = true

  kind_config {
    kind        = "Cluster"
    api_version = "kind.x-k8s.io/v1alpha4"

    node {
      role = "control-plane"

      # Host ports 80/443 reach NodePorts 30080/30443, where the gateway (week 3) listens.
      # This is how "localhost" traffic gets into a cluster that lives inside Docker.
      extra_port_mappings {
        container_port = 30080
        host_port      = var.http_port
        protocol       = "TCP"
      }
      extra_port_mappings {
        container_port = 30443
        host_port      = var.https_port
        protocol       = "TCP"
      }
    }

    dynamic "node" {
      for_each = range(var.worker_count)
      content {
        role = "worker"
      }
    }
  }
}
