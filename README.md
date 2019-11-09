# Despliegue Automático de Aplicaciones
## Creación de Imágenes Docker
Para crear el ambiente donde simular un proceso de despliegue contínuo se crean
las siguientes imágenes de docker que será la infraestructura necesaria para
implementarlo.

Las imágenes se pueden crear por medio de los comandos siguientes:
```
docker build -t db               infraestructura/postgres/
docker build -t app              infraestructura/payara/
docker build -t git              infraestructura/git/
docker build -t jenkins          infraestructura/jenkins/
docker build -t jsf-apps-builder infraestructura/jsf-apps-builder/
```

## Puesta en marcha
### Infraestructura
Se trabajará con los siguientes contenedores:

| Contenedor |                Propósito                   | Imagen  | Dirección IP |
|------------|--------------------------------------------|---------|--------------|
|     db     | Servidor de base de datos (PostgreSQL)     |   db    |  172.17.0.2  |
|    app     | Servidor de aplicaciones (Payara)          |   app   |  172.17.0.3  |
|    git     | Servidor de control de versiones (Git)     |   git   |  172.17.0.4  |
|   jenkins  | Servidor de integración contínua (Jenkins) | jenkins |  172.17.0.5  |

> Para obtener las mismas direcciones ip (o similares) deben crearse los contenedores
en el orden siguiente:

#### Servidor de Base de datos (PostgreSQL)
Para iniciar un contenedor nuevo a partir de la imagen de postgres, se ejecuta:
```
docker run --name db -d db
```
En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el
comando:
```
docker start db
```

#### Servidor de Aplicaciones (Payara)
Iniciar un nuevo contenedor:
```
docker run -p 4848:4848 -d --name=app app
```
Las credenciales para acceder a la consola de administración son:
- Nombre de usuario: admin
- Contraseña: admin

En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el
comando:
```
docker start app
```

#### Control de versiones
Debe ejecutarse el siguiente comando:
```
docker run -it --name=git git /bin/bash
```
En caso de ya tener creado el contenedor, se puede volver a iniciar mediante el
comando:
```
docker start -i git
```
Cada vez que se inicia el contenedor debe ejecutarse el servicio ssh para enviar
los cambios realizados, mediante el comando:
```
service ssh start
```

#### Servidor de Integración Continua (Jenkins)
##### Instalación
Para crear el contenedor se ejecuta el siguiente comando:
```
docker run -u root -d --name jenkins -v /var/run/docker.sock:/var/run/docker.sock jenkins
```
Una vez creado el contenedor, generamos las claves ssh del mismo utilizando los
siguientes comandos:
```
ssh-keygen
```
Al solicitar un nombre de archivo para guardar la clave y una frase para desencriptar
la clave tecleamos <ENTER> para aceptar el nombre de archivo por defecto y una
frase vacía.

Luego procedemos a copiar nuestra clave a los servidores:
- Servidor de control de versiones: para descargar los cambios al repositorio una
vez que se inicie un proceso de despliegue (manual o automático)
- Servidor de aplicaciones Payara para realizar el despliegue del ejecutable una
vez se tenga listo.

Esto lo haremos mediante los siguientes comandos
```
ssh-copy-id jenkins@172.17.0.3 #ip servidor de aplicaciones
ssh-copy-id jenkins@172.17.0.4 #ip servidor de control de versiones
```

> Las contraseñas del usuario se encuentran en los archivos Dockerfile correspondientes
a los contenedores de dichos servicios.

En el caso de tener creado el contenedor, se puede iniciar con el siguiente
comando:
```
docker start jenkins
```

##### Configuración
Para configurar el servidor, realizamos los siguientes pasos:
- Cargar la página del servidor de integración contínua (http://172.17.0.5:8080/)
en un navegador web
- Desbloquear el servidor Jenkins ingresando en el cuadro de texto, el contenido
del archivo `/var/jenkins_home/secrets/initialAdminPassword`
- En la pantalla Customize Jenkins, seleccionar la opción *Select plugins to install*
- En la siguiente pantalla seleccionar los siguientes plugins y dar click en Install
  - *Folders*
  - *OWASP Markup Formatter*
  - *Build Timeout*
  - *Credentials Binding*
  - *Timestamper*
  - *Workspace Cleanup*
  - *Pipeline*
  - *Pipeline: Stage View*
  - *Git*
- En la siguiente pantalla crear un usuario para el servidor Jenkins y dar click
en el botón *Save and continue*
  - Username: admin
  - Password: admin
  - Full name: Administrador
  - Email address: admin@example.com
- En la siguiente pantalla configurar la URL de Jenkins dar click en *Save and finish*

### Configuración para Peticiones HTTP de Despliegue
Con el objetivo de automatizar el despliegue de aplicaciones, debemos configurar
el servidor Jenkins para que acepte peticiones HTTP para desplegar los proyectos,
para ello debemos realizar las siguientes configuraciones:
* Generar un token para el API de Jenkins: Esto evita tener que enviar la contraseña
del usuario a utilizar para la autenticación de las peticiones
  * Ingresar al servidor Jenkins con el usuario y contraseña configurados
  * Dar click en la opción *Manage Jenkins* del menú
  * Elegir la opción *Manage Users*
  * Dar click en la opción *Create User*
  * Crear un usuario para realizar los despliegues, podemos hacerlo con los siguientes datos:
    * Username: ProductosApp
    * Password: productosapp
    * Full name: Despliegues ProductosApp
    * E-mail address: despliegues-productosapp@example.com
  * Cerrar sesión de Jenkins
  * Ingresar a Jenkins con los datos del usuario ProductosApp
  * Dar click en el nombre de usuario en la esquina superior derecha
  * Ir a la opción *Configure* del menú
  * Dar click en el botón *Add new token*
  * Dar click en el botón *Generate*
  * Copiar el token ya que este servirá para configurar el repositorio del proyecto.

## Automatización del despliegue de ProductosApp
Debemos seguir los siguientes pasos para crear nuestra definición de depliegue (pipeline) para ProductosApp:
- Ingresar al servidor Jenkins
- Dar click a la opción *New Item*
- Ingresar el nombre del proyecto (ProductosApp) y seleccionar la opción *Pipeline*
- Dar click en el botón *OK*.

Ya en el proyecto se deben realizar las siguientes configuraciones:
* Sección *Build Triggers*
  * Seleccionar la opción *Trigger builds remotely* con un token para autenticación (productos-token)
* Sección *Pipeline*
  * Seleccionar la opción *Pipeline script from SCM* en el campo Definition.
  * En la opción SCM seleccionar *Git*
  * En Repositoriy URL ingresar ssh://jenkins@*IP-Git*/home/git/app
  * En Script Path ingresar el nombre del archivo de definición del despliegue (*Jenkinsfile*)
* Dar click en *Save*
* Modificar el script `post-receive` de ProductosApp (`proyecto/post-receive`)
  * `tokenUsuario`: el token generado para el usuario ProductosApp
  * `urlJenkins`: la dirección del servicio Jenkins (incluyendo http o https)
  * `ProductosAppToken`: token asignado al proyecto ProductosApp de Jenkins
* Copiar el archivo `post-receive` a la dirección de *hooks* del repositorio de la aplicación (`/home/git/app/hooks`) en el contenedor de git.
