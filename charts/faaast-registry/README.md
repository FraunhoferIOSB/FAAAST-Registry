# FA³ST Registry [![Documentation Status](https://readthedocs.org/projects/faaast-registry/badge/?version=latest)](https://faaast-registry.readthedocs.io/en/latest/?badge=latest)

![FA³ST Registry Logo Light](https://github.com/FraunhoferIOSB/FAAAST-Registry/blob/main/docs/source/images/Fa3st-Registry_positiv.png/#gh-light-mode-only "FA³ST Registry Logo")
![FA³ST Registry Logo Dark](https://github.com/FraunhoferIOSB/FAAAST-Registry/blob/main/docs/source/images/Fa3st-Registry_negativ.png/#gh-dark-mode-only "FA³ST Registry Logo")

Helm chart for the **F**raunhofer **A**dvanced **A**sset **A**dministration **S**hell **T**ools (**FA³ST**) Registry.

For more details on FA³ST Registry see the full documentation :blue_book: [here](https://faaast-registry.readthedocs.io/).

| FA³ST Registry is still under development. Contributions in form of issues and pull requests are highly welcome. |
|------------------------------------------------------------------------------------------------------------------|

## Configuration

If you want to bring existing FA³ST Registry configuration to helm, convert your .properties file into .yaml format, 
then paste it into the faaast-registry section of your values.yaml file.

Note, that the Kubernetes Service created for the Registry maps the Registry port to the Registry port. If the 
Registry is configured to run on port `8090`, the Service maps `8090:8090`. The ingress will map to the service at 
`8090` as well.
