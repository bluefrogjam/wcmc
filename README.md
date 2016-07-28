# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* Quick summary

Provides people with an easy way to load files and resources independant.

### How do I get set up? ###

* Summary of set up

<dependency>
    <groupId>edu.ucdavis.fiehnlab</groupId>
    <artifactId>loader</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

* Configuration

  @Autowired
  val loader: DelegatingResourceLoader = null

is all what should need to be done. To add additional loaders to the delegate loader, you can define beans like this in your spring configuration

  @Bean
  def recursiveDirectoryLoader:RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("src"))

  @Bean
  def recursiveDirectoryLoader2:RecursiveDirectoryResourceLoader = new RecursiveDirectoryResourceLoader(new File("test"))
