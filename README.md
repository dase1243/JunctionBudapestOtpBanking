# Junction Budapest problem: OTP Banking
Recommendation engine for ATM search based on workload for OTP Bank

This folder contains a solution for Junction partner's project.

## Features
In projects we used greedy algorithm. But in the future we consider to switch into scoring system.
The assignment problem is a fundamental combinatorial optimization problem. It consists of finding,
in a weighted bipartite graph, a matching of a given size, in which the sum of weights of the edges is a minimum.

## Description

First of all, a new round in UX when working with ATMs had to happen sooner or later; now people less and less want
spend time waiting in line and performing operations on slow, albeit safe, ATMs.

Our service combines the solution to both of these problems.

Firstly, in our application it is possible to complete all operations in advance, so that upon arrival at the ATM,
you can complete them in a very small amount of time.

Secondly, in our application we introduce a recommendation system for selecting ATMs that are currently
least loaded and able to fully serve customer requests.
The queue is taken into account and on average 1.5 minutes are allocated to each

As a bonus, we solve the UX problem by reducing dissatisfaction with waiting in line.

Let's take a closer look at the ux of our application. Since now most applications are capable of banking
online operations, then we omitted them. We are interested in cash transactions.

First, the user is asked to set filters for selecting ATMs: either put money, then you need to look for those
in which there is such a function. Or the user wants to withdraw cash, and then we must filter by availability
enough money in atm.

After that, he gets to the screen with the listing, where the listing is sorted by priority, which was calculated by our algorithm
Now, to build a route to the desired ATM, the user only needs to click on the one he needs. If he chooses not
first on the list, we suggest that he choose another, because we are able to save several minutes of his time.

In addition, the user can go to the map view, where the route will already be laid to the first atm priority

Next, the following logic is connected - if the desired ATM is within a radius of 5 meters, then the application will offer to go to
further action. However, you can also go there yourself

When the user will be at the ATM, two outcomes are possible.
1) There is no queue, so the user can immediately scan the QR code and complete all operations.
2) The second situation is less pleasant when there is still a queue. We got the idea to offer the user to play a game, but at the same time
he has the opportunity to win coupons for various discounts or products from partners of the bank. For example, a discount on a trip to Lime.
Then, when the queue comes up, the user can go back and scan the QR code.

Now let's talk in more detail about QR codes. They provide solutions to many ATM issues. One of the most important is
user protection through authentication in several layers, because now most gadgets are equipped with a fingerprint scanner
finger and face id, which definitely surpasses the pin code in security. It will also prevent ATM from becoming more expensive, because then there is no need to embed them.

later, when firebase is added, a real-time opportunity will appear
change the route, because maybe someone who lives closer could take a turn in front of you
and then it’s easier for you to go to the next atm

later a server can be connected where the calculation will take place not only offline
users who were counted based on a previous story, but also had the opportunity to consider
online users who use online banking just like you

our solution is not super extraordinary so what makes us better than others? We know how tedious it is to stand in line while someone is very
slow will fulfill its needs, so to brighten up this time we give the user to win coupons from partners of the bank.

What does it give as a whole?
It is clear that it is impossible to fully foresee all the cases in which the user becomes dissatisfied.
Because of this, we look more at how we can smooth out the bad impression of that situation.
As an example, you can hang mirrors in elevators for skyscrapers.
Or the trick of an airline that offers a customer a deal that if, in 60 seconds, technical support doesn’t pick up,
then the company will provide a discount on the flight. And then people look forward to exceeding 60 seconds

However, we do not place a strong emphasis on the game, since we believe that this is only a transitional stage.
After all, as our service is introduced, more and more people will perform all the operations in advance, respectively, the atm load will become less than the current one.


