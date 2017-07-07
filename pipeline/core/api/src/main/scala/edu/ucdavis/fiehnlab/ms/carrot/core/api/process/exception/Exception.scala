package edu.ucdavis.fiehnlab.ms.carrot.core.api.process.exception

@Deprecated
class SampleRequiresTrackingButNotProvidedException( val message:String) extends Exception(message)

@Deprecated
class SampleRequiresNoTrackingButWasProvidedException( val message:String) extends Exception(message)
