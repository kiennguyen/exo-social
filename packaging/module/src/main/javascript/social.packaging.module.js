eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params)
{


  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
  var ws = params.ws;
  var jcr = params.eXoJcr;
  
  var module = new Module();
  
   module.version = "${project.version}"; 
   module.relativeMavenRepo = "org/exoplatform/social";
   module.relativeSRCRepo = "social";
   module.name = "social";
 
var shindigVersion = "${org.shindig.version}";  
	
  module.component = {} ;
  module.component.common = 
		new Project("org.exoplatform.social", "exo.social.component.common","jar", module.version);
  
  module.component.people = 
	new Project("org.exoplatform.social", "exo.social.component.people","jar", module.version);

  module.component.space = 
	new Project("org.exoplatform.social", "exo.social.component.space","jar", module.version);

  module.component.opensocial = 
	new Project("org.exoplatform.social", "exo.social.component.opensocial","jar", module.version);

  module.component.exosocial = 
	new Project("org.exoplatform.social", "exo.social.component.exosocial","jar", module.version);

  
  module.web = {};
	
  module.web.eXoResources = new Project("org.exoplatform.social", "exo.social.web.socialResources", "war", module.version);
  module.web.eXoResources.deployName = "eXoResourcesSocial" ;
	
  module.portlet = {}
  module.portlet.space = new Project("org.exoplatform.social", "exo.social.portlet.space", "exo-portlet", module.version);
  module.portlet.space.deployName = "space" ;

  module.portlet.profile = new Project("org.exoplatform.social", "exo.social.portlet.profile", "exo-portlet", module.version);
  module.portlet.profile.deployName = "profile" ;

  module.webui = {};
  module.webui.social = new Project("org.exoplatform.social", "exo.social.webui.social", "jar", module.version);

  module.application = {}
  module.application.rest = new Project("org.exoplatform.social", "exo.social.application.rest","jar", module.version).
	addDependency(ws.frameworks.json);  	

  module.web.opensocial =new Project("org.exoplatform.social", "exo.social.web.opensocial", "war", module.version).
		addDependency(new Project("commons-betwixt", "commons-betwixt", "jar", "0.8")).
		addDependency(new Project("net.sf.json-lib", "json-lib", "jar", "2.2")).
		addDependency(new Project("org.gatein.shindig", "shindig-social-api", "jar", shindigVersion)).
		addDependency(new Project("org.apache.geronimo.specs", "geronimo-stax-api_1.0_spec", "jar", "1.0.1"));
  module.web.opensocial.deployName = "social" ;

  module.extras = {};
  module.extras.feedmash = new Project("org.exoplatform.social", "exo.social.extras.feedmash", "jar", module.version);
  
  
	module.extension = {};
	module.extension.war =
   new Project("org.exoplatform.social", "exo.social.extension.war", "war", module.version).
   addDependency(new Project("org.exoplatform.social", "exo.social.extension.config", "jar", module.version));
	module.extension.war.deployName = "social-ext";

   module.demo = {};
   // demo portal
   module.demo.portal = 
	   new Project("org.exoplatform.social", "exo.social.demo.war", "war", module.version).
		addDependency(new Project("org.exoplatform.social", "exo.social.demo.config", "jar", module.version));
	   module.demo.portal.deployName = "socialdemo";  
	   
   // demo rest endpoint	   
   module.demo.rest = 
       new Project("org.exoplatform.social", "exo.social.demo.rest-war", "war", module.version);
       module.demo.rest.deployName = "rest-socialdemo"; 

       
       
       
   module.server = {}

   module.server.tomcat = {}
   module.server.tomcat.patch =
   new Project("org.exoplatform.social", "exo.social.server.tomcat.patch", "jar", module.version);

   module.server.jbossear = {}
   module.server.jbossear.patch =
   new Project("org.exoplatform.social", "exo.social.server.jboss.patch-ear", "jar", module.version);

  return module;

}
