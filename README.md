# ArkCase

ArkCase aims to be the leading open source case management and IT modernization platform. After supporting numerous case management and IT modernization initiatives, the team at Armedia developed a framework to accelerate these initiatives and to reduce the cost of implementation.  That framework matured and is the basis for ArkCase.  We have and will continue to invest in making ArkCase a premier platform for IT modernization.  As a thank you to our customers who embarked on this journey with us and a thank you to all the software engineers that have contributed open source technologies to advance this industry, ArkCase is now open source!

## Architecture

The ArkCase architecture is described here: https://www.arkcase.com/developer-support/architecture/.  

You can visit https://www.arkcase.com for more information on ArkCase in general.

## Run ArkCase in a Standalone VM

To evaluate ArkCase, or try it out, or just see how it works; in short, to run ArkCase in a VM without having to install all the developer tools, just follow the directions here: https://github.com/ArkCase/arkcase-ce#if-you-just-want-to-download-a-pre-built-arkcase-virtual-machine-and-run-arkcase

And you can skip the Developer Setup section below.

## Developer Setup

This section documents how developers can build and run ArkCase.  (For non-developers, and anyone who just wants to run ArkCase, please read the above section; you don't need to follow the rest of this wiki).

### Prerequisites

* Balena Etcher (balena.io/etcher)

### Configuring the environment to run ArkCase

In this section you will configure the enviroment to run ArkCase. To configure the environment follow the steps below.
* Start by installing Ubuntu, you should obtain the Ubuntu ISO file by downloading it from the following link
http://ubuntu.com/download/desktop

Create a bootable USB.
*Plug in your USB drive to your computer.
*Open Balena Etcher.
*Click on `Flash from file` and select the ISO file you want to make bootable.
* Click on `Select target` and choose your USB drive from the list.
* Click on `Flash` to start the process.
* Balena Etcher will show a warning that all data on the USB drive will be destroyed. Make sure you have backed up any important data and click `yes` to proceed.
* Balena Etcher will format the USB drive and copy the ISO contents, creating a bootable USB drive.
* Once Balena Etcher finishes, safely eject the USB drive from your computer. It is now bootable with the specified ISO image.
* After creating the bootable USB drive you can install the operating system on your computer.

Clone the following repositories.
```bash
https//github.com/ArkCase/ark_k8s_init.git
https://github.com/ArkCase/artifacts-dev.git
```

Once the repositories are cloned, execute the init script.
*Execute the following command in the terminal
```bash
cd path/to/ark_k8s_init
./ubuntu-k8s-init
```

After the script executed successfully, Ensure the availability of essential commands: 
*helm 
*kubectl 
*docker

Run the following command to add your user to the docker group
```bash
Sudo usermod -aG docker $USER
```

Add the cluster configuration to the current user
*Create a directory named .kube under the home directory
```bash
Mkdir ~/.kube
```
*Copy the cluster configuration to your user’s .kube directory
```bash
Sudo cat /etc/Kubernetes/admin.conf>~/.kube/config
```

Finally verify the cluster pods status using 
```bash
sudo kubectl get pods -A
```
Running this comand should return all the cluster pods that are currently running.

### Install ArkCase with helm chart

To install ArkCase using helm chart we need to add the helm chart to our local repository.
*Run the following command to add arkcase to repository and to update it. 
```bash
helm repo add arkcase https://arkcase.github.io/ark_helm_charts/
helm repo update
```
### Deploying ArkCase CE

Now that the repository is installed and updated we can proceed to deploy the ArkCase CE.
```bash
helm install arkcase arkcase/app
```
### Access ArkCase from browser
To access ArkCase from browser we need to port forward the service. For that to be done, get the cluster IP of the `core` service.
*Use this command to get the cluster IP: `kubectl get service core` (Note the CLUSTER-IP).
*Port forward the service: `kubectl port-forward service/core 8443:8443`
*Access ArkCase through `Cluster-IP:8443` in your browser.

### Logging into ArkCase

Once you see the ArkCase login page, you can log in with the default administrator account.  User `arkcase-admin@arkcase.org`, password `@rKc@3e`.
