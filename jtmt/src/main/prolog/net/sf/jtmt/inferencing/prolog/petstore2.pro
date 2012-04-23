%% Source: petstore2.pro
%% Drools Petstore Example rewritten to work as an Inference Engine service
%% from a Java JPL client. The Java client makes successive calls to checkout,
%% until the goal is successful. If unsuccessful, the inference engine will
%% assert a question into the factbase. The client must query the factbase
%% for the question to be answered and pass in the answer in a subsequent
%% call to checkout. Because we typically want to invoke the engine in a
%% stateless manner, all previously answered questions are held by the client
%% and are passed in during successive calls, along with the current answer.
%% When all answers are available, the inference engine runs to completion,
%% ie, the checkout goal is successful and asserts the updated cart into the
%% factbase, from which it should be retrieved by the client.

:- dynamic cart/1.
:- dynamic question/1.
:- dynamic answer/2.

% A shopping cart is minimally represented as a sequence of quantities
% of [fish, food, tank]. Elements subsequent to that are answers to
% questions that the engine has asked and which it requires answers to
% in order to proceed. If provided, they are asserted into the factbase.
itemize(Cart, Fish, Food, Tank) :-
  [Fish, Food, Tank | Q] = Cart,
  extract_info(Q).
extract_info([H|R]) :-
  assert(H),
  extract_info(R).
extract_info([]) :- !.

% Add 1 packet of food for every 5 fish purchased.
add_free_food(Fish, FreeFood) :-
  FreeFood is floor(Fish / 5).

% Depending on customer's feedback, add more fish food to shopping cart.
% Only do this if the customer has bought some fish. If he is here to
% buy only food or a tank, don't even ask him about it.
add_more_food(Fish, MoreFood) :-
  Fish =< 0,
  MoreFood is 0,
  !.
add_more_food(Fish, MoreFood) :-
  Fish > 0,
  not(answer(how_many_food, _)),
  assert(question(how_many_food)),
  MoreFood is 0,
  fail.
add_more_food(Fish, MoreFood) :-
  Fish > 0,
  answer(how_many_food, MoreFood).

% If customer has bought 10 or more fish and no tank to put them in,
% ask if he wants a fish tank. If answer is already provided, add
% fish tank (or not if he says no) to cart.
add_fish_tank(Fish, _, AddTank) :-
  Fish < 10,
  AddTank is 0,
  !.
add_fish_tank(_, Tank, AddTank) :-
  Tank > 0,
  AddTank is 0,
  !.
add_fish_tank(Fish, Tank, AddTank) :-
  Fish >= 10,
  Tank == 0,
  not(answer(add_a_tank, _)),
  assert(question(add_a_tank)),
  AddTank is 0,
  fail.
add_fish_tank(Fish, Tank, AddTank) :-
  Fish >= 10,
  Tank == 0,
  answer(add_a_tank, AddTank).

% Apply a 10% discount on orders over $50.
apply_discount(Total, Discount) :-
  Total < 50,
  Discount is 0.
apply_discount(Total, Discount) :-
  Total >= 50,
  Discount is 0.1 * Total.

%checkout(SCart) :-
%  concat_atom(Cart, :, SCart),
%  checkout(Cart).

% The main goal which is called from the Java client. Applies the sub-goals
% in sequence, failing if there is not enough information to go ahead. If
% all information is provided, then populates a new shopping cart and asserts
% it into the factbase, from which it should be retrieved by the client.
checkout(Cart) :-
  retractall(answer(_, _)),
  retractall(question(_)),
  retractall(cart(_)),
  itemize(Cart, Fish, Food, Tank),
  add_free_food(Fish, FreeFood),
  add_more_food(Fish, MoreFood),
  TotalFood is Food + MoreFood,
  add_fish_tank(Fish, Tank, AddTank),
  Total is (5 * Fish) + (2 * TotalFood) + (40 * AddTank),
  apply_discount(Total, Discount),
  append([], [Fish, TotalFood, AddTank, FreeFood, Discount], NewCart),
  assert(cart(NewCart)),
  !.
