# Despliegue Automático de Aplicaciones
En el siguiente documento se describen los pasos necesarios para realizar una
prueba de despliegue automatizado para una aplicación Java web con JavaServer
Faces (JSF.)

## Requisitos
Es necesario tener instalado Docker para ejecutar la infraestructura necesaria
(máquinas virtuales) del siguiente proceso.

### Docker
Permite la creación de ambientes para ejecución de aplicaciones (es un tipo de
virtualización). Lo más importante para este proyecto es que permite construir
contenedores (algo así como máquinas virtuales livianas) a partir de plantillas
(imágenes.)

> Documentación sobre conceptos de Docker se pueden conseguir en las siguientes
URLs: `https://docs.docker.com/get-started/` y `https://docs.docker.com/engine/docker-overview/`

> Un manual para instalación de Docker en sistemas basados en Debian/Linux se encuentra en la siguiente URL:
`https://docs.docker.com/install/linux/docker-ce/debian/`

## Infraestructura
Se trabajará con los siguientes contenedores (servidores) configurados con las
direcciones ip descritas en la siguiente tabla:

| Contenedor |                Propósito                   | Imagen  | Dirección IP |
|------------|--------------------------------------------|---------|--------------|
|     db     | Servidor de base de datos (PostgreSQL)     |   db    |  172.17.0.2  |
|    app     | Servidor de aplicaciones (Payara)          |   app   |  172.17.0.3  |
|    git     | Servidor de control de versiones (Git)     |   git   |  172.17.0.4  |
|   jenkins  | Servidor de integración contínua (Jenkins) | jenkins |  172.17.0.5  |

### Creación de las imágenes de los contenedores
Para crear las imágenes de los contenedores que utilizaremos como base para los
servidores que necesitamos, primero debemos abrir una terminal y cambiarnos al
directorio raíz del proyecto (ej
`cd /home/usuario/Desktop/despliegue_aplicaciones`) y luego ejecutar los siguientes comandos:
```
docker build -t       db         infraestructura/postgres/
docker build -t       app        infraestructura/payara/
docker build -t       git        infraestructura/git/
docker build -t     jenkins      infraestructura/jenkins/
docker build -t jsf-apps-builder infraestructura/jsf-apps-builder/
```

### Inicialización de servidores
Para obtener las mismas direcciones ip (o similares) deben crearse los contenedores
en el orden siguiente:
> Hay que tomar en cuenta que las direcciones en el documento se encuentran en
relación a la tabla descrita en la sección Infraestructura, por lo tanto, si no
obtenemos las mismas direcciones debemos tener cuidado de escribir las direcciones
correctas en los pasos/scripts/archivos de configuración.

#### Servidor de Base de datos
Si no hemos creado el servidor de base de datos, podemos hacerlo con el siguiente
comando, el cual creará un contenedor con nombre `db` a partir de la imagen `db`:
```
docker run --name db -d db
```
En caso de ya tener creado el contenedor, se puede iniciar mediante el comando:
```
docker start db
```

#### Servidor de Aplicaciones
De manera similar, para crear el servidor de aplicaciones podemos hacerlo con el
siguiente comando, el cual creará un contenedor con nombre `app` a partir de la
imagen `app` exponiendo el puerto `4848` del contenedor a través del puerto `4848`
de nuestra máquina local:
```
docker run -p 4848:4848 -d --name=app app
```
> En el caso del servidor de aplicaciones, es necesario exponer el puerto de
administración (`4848`) debido a que se puede acceder a él de manera local al
crear el servidor.

> Para acceder a la consola de administración del servidor de aplicaciones debemos
abrir un navegador web y dirigirnos a la dirección de la máquina donde se esté
ejecutando Docker con el puerto 4848 (ej. `https://localhost:4848`) e introducir
las siguientes credenciales:
- Nombre de usuario: admin
- Contraseña: admin

En caso de ya tener creado el contenedor, se puede iniciar mediante el comando:
```
docker start app
```

#### Servidor de Control de versiones
Si no hemos creado el servidor de control de versiones, podemos hacerlo con el
siguiente comando, el cual creará un contenedor con nombre `git` a partir de la
imagen `git` y nos dará una terminal bash:
```
docker run -it --name=git git /bin/bash
```
> Lo importante con este contenedor, es que estará activo siempre que no cerremos
la terminal que nos provee, por lo tanto, hay que evitar ingresar el comando `exit`
ya que apagará el servidor. Si por accidente lo apagamos podemos iniciarlo según
la siguiente explicación

En caso de ya tener creado el contenedor, se puede iniciar mediante el comando:
```
docker start -i git
```
> Cada vez que se inicia el contenedor debe ejecutarse el servicio `ssh` mediante
el siguiente comando, para registrar y consultar los cambios realizados:
```
service ssh start
```

#### Servidor de Integración Continua
##### Instalación
Si no hemos creado el servidor de integración contínua, podemos hacerlo con el
siguiente comando, el cual creará un contenedor con nombre `jenkins` a partir de
la imagen `jenkins`:
```
docker run -u root -d --name jenkins -v /var/run/docker.sock:/var/run/docker.sock jenkins
```
> Como se puede apreciar se monta un volumen hacia el archivo /var/run/docker.sock
desde el contenedor de integración contínua, esto es necesario ya que debido a
que este contenedor creará un contenedor adicional para compilar la aplicación,
se configuró de modo que utilice la instancia de Docker de la máquina anfitrión
y no tener que instalar Docker en ese contenedor también (además es la recomendación
de la documentación de Jenkins.)

Una vez creado el contenedor, generamos las claves ssh del mismo utilizando el
siguiente comando:
```
ssh-keygen
```
> Al solicitar un nombre de archivo para guardar la clave y una frase para desencriptar
la clave tecleamos \<ENTER\> para aceptar el nombre de archivo por defecto y una
frase vacía.

> Esto con la finalidad de registrar la identidad del servidor de integración
contínua en los servidores de aplicaciones y de control de versiones para ejecutar
comandos de forma remota via ssh sin necesidad de interacción para realizarlo.

Luego procedemos a copiar la identidad del servidor (clave ssh) a los servidores:
- Servidor de control de versiones: para obtener los cambios hechos al repositorio
una vez que se inicie un proceso de despliegue (manual o automático)
- Servidor de aplicaciones: para realizar el despliegue del ejecutable una vez
se tenga listo el mismo.

Esto lo haremos mediante los siguientes comandos
```
# copiado de identidad al servidor de aplicaciones (app)
ssh-copy-id jenkins@172.17.0.3
# copiado de identidad al servidor de control de versiones (git)
ssh-copy-id jenkins@172.17.0.4
```

> Las contraseñas del usuario jenkins se encuentran en los archivos Dockerfile
correspondientes a los contenedores de dichos servicios,
`infraestructura/payara/Dockerfile` e `infraestructura/git/Dockerfile`.

##### Configuración
Para configurar el servidor de integración contínua (web), realizamos los
siguientes pasos:
- Cargar la página del servidor (http://172.17.0.5:8080/) en un navegador web
- Desbloquear el servidor Jenkins ingresando en el cuadro de texto, el contenido
del archivo `/var/jenkins_home/secrets/initialAdminPassword`
- Para este ejercicio necesitamos solamente algunos de los plugins, por lo tanto:
  - En la pantalla Customize Jenkins, seleccionar la opción *Select plugins to
    install*
    - En la siguiente pantalla seleccionar los siguientes plugins y dar click en
      *Install*
      - *Folders*
      - *OWASP Markup Formatter*
      - *Build Timeout*
      - *Credentials Binding*
      - *Timestamper*
      - *Workspace Cleanup*
      - *Pipeline*
      - *Pipeline: Stage View*
      - *Git*
- En la siguiente pantalla crear un usuario con los siguientes datos y dar click
  en el botón *Save and continue*
  - Username: admin
  - Password: admin
  - Full name: Administrador
  - Email address: admin@example.com
- En la siguiente pantalla configurar la URL del servidor (http://172.17.0.5:8080)
  y dar click en *Save and finish*

##### Inicialización
En el caso de tener creado el contenedor, se puede iniciar con el siguiente
comando:
```
docker start jenkins
```
> Las configuraciones hechas en las secciones de instalación y configuración solo
se deben realizar una sola vez.

## Proceso de Despliegue Automatizado de ProductosApp
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
