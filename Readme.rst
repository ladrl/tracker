========
 Tracker
========

A minimalistic tracker, implemented with scala in a functional fashion. 

The API is designed after the metaphore of a library. 

	This is a library.
	Books can be created and put in it.
	Books are made from a front page with head lines and a number of pages inside.
	The catalogue can be used to find a particular book.
	Everything is undestroyable, one can only create modified copies but nothing can be changed.
	
Rationale
---------

 - I like trackers, but none of them is cool enough
 - Scala rocks and I learn lot's of FP
 - API design seems to be interesting too
 - ...
 - Just want it :-)


Consequences of immutability
----------------------------

A side-effect of the immutability in this API is the history which is recorded. When a book is mutated,
there must be some book before it (when creating one it's the 'EmptyBook'), normally this book is 
already in the library. Thus, if it's not explicitly removed, it remains and forms the history of the
book just created. To facilitate this, some means of traceing must be given (there must be no identical 
copies of two books).


Roadmap
-------

#. Create API, inclusive tests to define the semantics
#. Add DB backend, current candidate is mongodb
#. Add a cmd line interface to create a usable application (perhaps introduce OSGi at some point)
