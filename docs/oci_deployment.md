# Oracle Cloud Infrastructure (OCI) Deployment Guide
## KEC Student Sports Athletic Performance & Scholarship Evaluator

---

## 1. Cloud Deployment Overview

The project is structured as a two-node distributed enterprise deployment across two Compute Virtual Machines (VMs) in Oracle Cloud Infrastructure (OCI):

- **Student 1 (Application Host - OCI VM 1)**:
  - Deploys Apache Tomcat 9/10, OpenJDK 17, and the Maven application package (`kec-sports-evaluator.war`).
  - Executes multithreaded SPI calculations, String parsing, and `.txt` file export.
  - Connects remotely to the MySQL database on VM 2 over the private VCN subnet.

- **Student 2 (Database Host - OCI VM 2)**:
  - Deploys MySQL 8.0 Community Server.
  - Configures OCI VCN Ingress Rules, OS Firewalls (`firewalld`), and secure remote user grants.
  - Placed in a private subnet with no public IP for maximum security.

---

## 2. Cloud Network Architecture (VCN Configuration)

```
OCI Virtual Cloud Network (VCN): 10.0.0.0/16
│
├── Public Subnet: 10.0.1.0/24 (Contains OCI VM 1 - Application Host)
│   ├── Internet Gateway (IGW) Attached
│   ├── Route Table: 0.0.0.0/0 -> Internet Gateway
│   └── Security List:
│       • Ingress TCP 22 (SSH Management) from Administrator IP
│       • Ingress TCP 8080 (Web Dashboard) from 0.0.0.0/0
│
└── Private Subnet: 10.0.2.0/24 (Contains OCI VM 2 - Database Host)
    ├── NAT Gateway / Service Gateway Attached (No direct Internet ingress)
    └── Security List / NSG:
        • Ingress TCP 22 (SSH) from 10.0.1.10 only (Bastion / Jump)
        • Ingress TCP 3306 (MySQL) from 10.0.1.10/32 ONLY
        • ALL OTHER INGRESS: DENIED
```

---

## 3. Student 2: Database Host (OCI VM 2) Deployment Steps

### Step 2.1: Provisioning VM 2 in OCI Console
1. Log into OCI Console (`cloud.oracle.com`).
2. Navigate to **Compute** -> **Instances** -> **Create Instance**.
3. Name: `kec-sports-db-vm2`.
4. Image: **Oracle Linux 8** (or Ubuntu 22.04 LTS).
5. Shape: `VM.Standard.E4.Flex` (1 OCPU, 4 GB RAM) or `VM.Standard.A1.Flex`.
6. Networking: Select your VCN -> Select **Private Subnet** (Assigned Private IP: `10.0.1.20`).
7. Paste your SSH public key and click **Create**.

### Step 2.2: OCI VCN Ingress Rule Configuration
1. Navigate to **Networking** -> **Virtual Cloud Networks** -> Click your VCN.
2. Select **Security Lists** -> Click **Security List for Private Subnet**.
3. Click **Add Ingress Rules**:
   - **Stateless**: No (Stateful)
   - **Source Type**: CIDR
   - **Source CIDR**: `10.0.1.10/32` *(OCI VM 1 Application Host Private IP)*
   - **IP Protocol**: TCP
   - **Source Port Range**: All
   - **Destination Port Range**: `3306`
   - **Description**: `Allow MySQL access strictly from Application Host VM 1`
4. Click **Add Ingress Rules**.

### Step 2.3: OS Firewall Setup on VM 2
Log into VM 2 via SSH and run:

```bash
# Verify firewalld is active
sudo systemctl status firewalld

# Add firewall rich rule allowing TCP 3306 exclusively from VM 1 Private IP
sudo firewall-cmd --permanent --zone=public --add-rich-rule='rule family="ipv4" source address="10.0.1.10" port port="3306" protocol="tcp" accept'

# Reload firewalld to apply changes
sudo firewall-cmd --reload

# Verify active firewall rules
sudo firewall-cmd --list-all
```

*Expected firewalld output:*
```
public (active)
  target: default
  icmp-block-inversion: no
  interfaces: ens3
  sources: 
  services: ssh
  ports: 
  protocols: 
  forward: no
  masquerade: no
  forward-ports: 
  source-ports: 
  icmp-blocks: 
  rich rules: 
	rule family="ipv4" source address="10.0.1.10" port port="3306" protocol="tcp" accept
```

---

## 4. Student 1: Application Host (OCI VM 1) Deployment Steps

### Step 4.1: Provisioning VM 1 in OCI Console
1. Navigate to **Compute** -> **Instances** -> **Create Instance**.
2. Name: `kec-sports-app-vm1`.
3. Image: **Oracle Linux 8** or **Ubuntu 22.04 LTS**.
4. Networking: Select your VCN -> Select **Public Subnet** (Assigned Private IP: `10.0.1.10`, Public IP allocated).
5. Add Ingress Rule to Public Subnet Security List:
   - **Protocol**: TCP, **Port Range**: `8080`, **Source**: `0.0.0.0/0` (HTTP Web Dashboard).

### Step 4.2: Installing JDK 17 & Tomcat 9/10 on VM 1
```bash
# Install Java 17 OpenJDK
sudo dnf install java-17-openjdk java-17-openjdk-devel git maven -y

# Verify Java
java -version

# Install Apache Tomcat
sudo useradd -m -U -d /opt/tomcat -s /bin/false tomcat
cd /tmp
wget https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.96/bin/apache-tomcat-9.0.96.tar.gz
sudo tar -xf apache-tomcat-9.0.96.tar.gz -C /opt/tomcat --strip-components=1
sudo chown -R tomcat:tomcat /opt/tomcat
sudo chmod -R u+x /opt/tomcat/bin
```

### Step 4.3: Configuring Remote Connection in `application.properties`
On VM 1, edit `src/main/resources/application.properties`:
```properties
db.url=jdbc:mysql://10.0.1.20:3306/kec_sports_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.username=kec_sports_admin
db.password=SecureKECPassword2026!
```

Or set environment variables:
```bash
export KEC_DB_URL="jdbc:mysql://10.0.1.20:3306/kec_sports_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export KEC_DB_USERNAME="kec_sports_admin"
export KEC_DB_PASSWORD="SecureKECPassword2026!"
```

### Step 4.4: Packaging and Deploying WAR
```bash
cd /path/to/kec-sports-evaluator

# Build production WAR file
mvn clean package

# Deploy WAR to Tomcat webapps
sudo cp target/kec-sports-evaluator.war /opt/tomcat/webapps/

# Start Tomcat service
sudo /opt/tomcat/bin/startup.sh
```

---

## 5. Remote Connectivity Verification & Handshake Proofs

Run these verification commands to prove the remote database handshake between VM 1 and VM 2:

### Proof 1: Netcat / Telnet Port Handshake from VM 1 to VM 2
```bash
# From VM 1: Test port 3306 reachability on VM 2
nc -zv 10.0.1.20 3306
# Or telnet:
telnet 10.0.1.20 3306
```
*Expected Output:*
```
Connection to 10.0.1.20 3306 port [tcp/mysql] succeeded!
```

### Proof 2: Direct Remote MySQL CLI Handshake from VM 1
```bash
mysql -h 10.0.1.20 -u kec_sports_admin -p -e "STATUS;"
```
*Expected Output:*
```
--------------
mysql  Ver 8.0.33 for Linux on x86_64 (Source distribution)

Connection id:          42
Current database:       kec_sports_db
Current user:           kec_sports_admin@10.0.1.10
SSL:                    Not in use
Server version:         8.0.33 MySQL Community Server - GPL
Protocol version:       10
Connection:             10.0.1.20 via TCP/IP
TCP port:               3306
--------------
```

### Proof 3: Active Socket Inspection on VM 2 (`netstat` / `ss`)
```bash
# On VM 2: View established connection from VM 1
sudo ss -tanp | grep 3306
```
*Expected Output:*
```
LISTEN    0      70     0.0.0.0:3306      0.0.0.0:*    users:(("mysqld",pid=1420,fd=31))
ESTAB     0      0      10.0.1.20:3306    10.0.1.10:48292 users:(("mysqld",pid=1420,fd=38))
```

---

## 6. Accessing the Cloud Application

Once deployed, access the dashboard at:
```
http://<VM1_PUBLIC_IP>:8080/kec-sports-evaluator/
```
Login using:
- **Username**: `admin`
- **Password**: `admin123`
