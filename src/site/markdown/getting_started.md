### Wicket-modelfactory is an API to create Wicket PropertyModels in a typesafe and refactoring-safe way.

Code says more than thousand words:

	IModel<Integer> ageModel = model(from(myPersonObject).getAge());

or 

	IModel<String> streetModel = model(from(myPersonObject).getAdressData().getHomeAdress().getStreet());      

where _myPersonObject_ can be a typed IModel or a Bean. 

You can also create Property Path-Strings in order to use them where PropertyModels are not helpful (PropertyColumns for instance).

	String streetPropertyPath = path(from(myPersonObject).getAdressData().getHomeAdress().getStreet());      

## Usage

#### Maven

add the following section to your pom.xml:

	  <dependency>
	   <groupId>org.wicketeer</groupId>
	   <artifactId>wicket-modelfactory</artifactId>
	   <version>0.9.13</version> <!-- or the latest released one -->
	  </dependency>

_model()_, _path()_ and _from()_ are static methods on ModelFactory. You should probably prepare your IDE to import these. Here is how this is done with Eclipse:

* go to Preferences Dialog
* Open Java / Editor / Content Assist / Favorites
* add Type org.wicketeer.modelfactory.ModelFactory


#### Tip

If you use the excellent [lombok|http://project-lombok.org] project, you end up with very little boilerplate and type-safe models:

	@Data class Bar{
	 private Foo foo;
	}
	@Data class Foo{
	 private String myString;
	}
	...
	IModel<Integer> ageModel = model(from(barInstance).getFoo().getMyString());

#### Kudos

The idea is pretty old. There are several implementations around, this lib steals from:

[SafeModel](http://github.com/duesenklipper/wicket-safemodel) 

[Wicket Wiki using LambdaJ](http://cwiki.apache.org/WICKET/working-with-wicket-models.html#WorkingwithWicketmodels-LambdaJ)

and probably some more...

The reason for this little project is that we liked the SafeModel-API (using a ThreadModel) better than the lambdaJ approach.

#### tl;dr;  

_SafeModel = nice but slow_

_LambdaJ = fast but a little more clumsy and sometimes JIT breaks it._ 

#### Longer version of the story:

Safemodel exposes a nicer API that makes use of ThreadLocals in order to chain calls.
On the other hand SafeModel happens to be too slow for us (We had a page with >600 models that went up from 150ms to 2700ms just by using SafeModel rather than lambdaJ.

LambdaJ on the other hand has a feature of JITting that, being enabled, caused spuriously intermixed models/failures.  



##### Implementation

We faciliated LambdaJ (without the JITing), Objenesis, GentyRef & CGLib.
 
While this list of dependencies may sound frightening, the only dependency you need that does not necessarily come with Wicket is *cglib:cglib-nodep:2.2.2*. (Note: If you use wicket-ioc, you'll have this dependency anyway.)

The rest of the dependencies are shaded into an internal package of Wicket-modelfactory, in order not to interfere with your dependecy management.

Unfortunately, this is not easy to do with CGLib, as it contains Strings referencing its classes.

##### All shaded libs are ASL 2.0 licensed, so is this thing. 
