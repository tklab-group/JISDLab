# JISDVis
The visualization tools supported for JISD
## Setup

1. Create external docker network `jisdlab-network`
  ```sh
  docker network create jisdlab-network
  ```
2. Build & start the container
  ```sh
  docker-compose up --build -d
  ```

## Containers
  - for data source
    - Elasticsearch (3 node)
    - Prometheus
  - for visualization
    - Grafana
    - Kibana
