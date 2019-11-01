# Despliegue Automático de Aplicaciones
## Creación de Imágenes Docker
Se deben crear las imágenes Docker para poner en marcha los contenedores necesarios, para ello, debemos posicionarnos en el directorio raíz del repositorio y ejecutar los siguientes comandos:
```
docker build -t db infraestructura/postgres/
docker build -t app infraestructura/payara/
docker build -t git infraestructura/git/
docker build -t jenkins infraestructura/jenkins/
```

## Configuración del anfitrión del servidor de aplicaciones
En el anfitrión que se utilizará para los contenedores del servidor de aplicaciones (Payara), debe configurarse de la siguiente manera:
- Crear el usuario jenkins
- Crear el directorio de aplicaciones a desplegar en el servidor de aplicaciones (`/payara-apps`)
- Habilitar el servicio ssh

La configuración descrita puede realizarse ejecutando el siguiente script en el anfitrión (con los permisos necesarios):
```
useradd -d/home/jenkins jenkins \
  && adduser jenkins docker \
  && mkdir /home/jenkins \
  && chown jenkins:jenkins /home/jenkins \
  && mkdir /payara-apps
  && chown jenkins:jenkins /payara-apps \
  && echo jenkins:jenkins | chpasswd
apt-get install -y openssh-server
```

## Puesta en marcha
### Creación de contenedores
#### Base de datos (PostgreSQL)
Para iniciar un contenedor nuevo a partir de la imagen de postgres, se ejecuta
`docker run --name db -d db`
En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el comando:
`docker start db`
Al iniciar el contenedor, este habilitará el servicio postgres para utilizar la base de datos.

#### Servidor de Aplicaciones (Payara)
Iniciar un nuevo contenedor
```
docker run -p 4848:4848 -d --name=app -v /payara-apps:/opt/payara/deployments app
```
Las credenciales para acceder a la consola de administración son:
- nombre de usuario: admin
- contraseña: admin

En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el comando:
```
docker start app
```
En cada despliegue, este servidor será regenerado por el servidor de integración contínua y desplegará las aplicaciones que se encuentren en el directorio `/payara-apps`

#### Control de versiones
Debe ejecutarse el siguiente comando
`docker run -it --name=git git /bin/bash`
En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el comando:
`docker start -i git`
Cada vez que se inicia el contenedor debe ejecutarse el servicio ssh para enviar los cambios realizados, mediante el comando:
`service ssh start`

#### Servidor de Integración Continua (Jenkins)
Debe ejecutarse el siguiente comando
```
docker run -it --name=jenkins jenkins /bin/bash
```
Una vez creado el contenedor, debemos generar las claves ssh y enviarlas al servidor de aplicaciones y de control de versiones mediante los siguientes comandos:
```
su - jenkins
ssh-keygen # al momento de crear el archivo aceptar los valores por defecto pulsando <ENTER>
ssh-copy-id jenkins@172.17.0.1 # docker host para contenedores payara, la constraseña se encuentra en el Dockerfile de payara
ssh-copy-id jenkins@172.17.0.4 # git, la contraseña se encuentra en el Dockerfile de git
```

En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el comando:
`docker start -i jenkins`

Cada vez que se inicie el contenedor debe iniciarse el servidor Jenkins mediante el comando:
`service jenkins start`

##### Instalación de Jenkins
La primera vez que ingresemos al servidor Jenkins, se deberá configurar el mismo, para lo cual
 - Cargar la página 172.17.0.5:8080 en un navegador web
 - Desbloquear el servidor Jenkins ingresando en el cuadro de texto, el contenido del archivo `/var/lib/jenkins/secrets/initialAdminPassword`
 - En la pantalla Customize Jenkins, seleccionar la opción Select plugins to install
 - En la siguiente pantalla seleccionar los siguientes plugins y dar click en Install
	 - Folders
	 - Build Timeout
	 - Credentials Binding
	 - Timestamper
	 - Workspace Cleanup
	 - Pipeline
	 - Pipeline: Stage View
	 - Git
 - En la siguiente pantalla crear un usuario para el servidor Jenkins y dar click en el botón Save and continue
	 - Username: admin
	 - Password: admin
	 - Full name: El usuario
	 - Email address: usuario@organizacion.com
 - En la siguiente pantalla configurar la URL de Jenkins que tendrá finalmente y dar click en Save and finish
	 - URL Jenkins: http://172.17.0.5:8080/

## Automatización del despliegue de ProductosApp
Debemos seguir los siguientes pasos para crear nuestra definición de depliegue (pipeline):
- Ingresar al servidor Jenkins
- Dar click a la opción New Item
- Ingresr el nombre del proyecto (ProductosApp) y seleccionar la opción Pipeline
- Dar click en el botón OK.

Ya en el proyecto se deben realizar las siguientes configuraciones:
* Sección Build Triggers
	* Seleccionar la opción Trigger builds remotely con un token para autenticación (productos-token)
* Sección Pipeline
	* Seleccionar la opción *Pipeline script from SCM* en el campo Definition.
	* En la opción SCM seleccionar Git
	* En Repositoriy URL ingresar ssh://jenkins@172.17.0.4/home/git/app
	* En Script Path ingresar el nombre del archivo de definición del despliegue (por defecto es Jenkinsfile)
* Dar click en Save

### Enlazar el Repositorio a Jenkins
Con el objetivo de automatizar el despliegue de aplicaciones, debemos notificar al servidor Jenkins que ha habido un cambio en el repositorio para que inicie el despliegue del proyecto, para lo cual debemos realizar las siguientes configuraciones:
* Generar un token para el API de Jenkins
	* Dar click en el nombre de usuario que se encuentra en la esquina superior derecha
	* Ir a la opción Configure del menú
	* Dar click en el botón Add new token
	* Copiar el valor generado y modificarlo en el archivo post-receive que se encuentra en la carpeta proyecto en la línea de la petición para realizar el despliegue en relación al parámetro usuario.
* Copiar el archivo post-receive que se encuentra en la carpeta proyecto a la dirección de hooks del repositorio (/home/git/app/hooks) en el contenedor de git
