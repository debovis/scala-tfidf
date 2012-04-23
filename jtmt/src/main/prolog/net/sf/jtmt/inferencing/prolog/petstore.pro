%% Source: petstore.pro
%% $Id$

%% Interactive version - meant to be run in the Prolog listener.

% A Cart is modelled as a sequence of:
% [#-fish, #-food, #-free-food, #-tank,discount_amount].
% The following block are rules to pull out components from the
% cart and populate the required value in the output parameter.
% @param Cart (IN) - the sequence representing the shopping cart.
% @param N (OUT) - the value of the named component.
number_of_fish([NumberOfFish, _, _, _, _], N) :- N is NumberOfFish.
number_of_food([_, NumberOfFood, _, _, _], N) :- N is NumberOfFood.
number_of_free_food([_, _, NumberOfFreeFood, _, _], N) :- N is NumberOfFreeFood.
number_of_tank([_, _, _, NumberOfTank, _], N) :- N is NumberOfTank.
discount([_, _, _, _, Discount], N) :- N is Discount.

% Computes the contents of the cart using the predefined prices
% @param Cart (IN) - the sequence representing the shopping cart.
% @param Total (OUT) - the computed total dollar value of the cart.
compute_cart_total(Cart, Total) :-
  number_of_fish(Cart, NumberOfFish),
  number_of_food(Cart, NumberOfFood),
  number_of_tank(Cart, NumberOfTank),
  Total is (5 * NumberOfFish) + (2 * NumberOfFood) + (40 * NumberOfTank).

% if customer has 5 or more fish, give him free fish food,
% 1 fish food packet for every 5 fish he purchases.
add_free_fish_food :-
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  number_of_food(Cart, NumberOfFood),
  number_of_tank(Cart, NumberOfTank),
  discount(Cart, Discount),
  NewFreeFood is floor(NumberOfFish / 5),
  append([], [NumberOfFish, NumberOfFood, NewFreeFood, NumberOfTank, Discount], NewCart),
  retract(cart(Cart)),
  assert(cart(NewCart)).

% ask customer if he wants additional fish food.
% @param Quantity (OUT) - the quantity of additional fish food.
ask_add_fish_food(Quantity) :-
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  ask_add_fish_food(NumberOfFish, Quantity).
ask_add_fish_food(0, Quantity) :-
  Quantity is 0.
ask_add_fish_food(NumberOfFish, Quantity) :-
  NumberOfFish > 0,
  write('>> How much more fish food to add? '), read(Quantity), nl.

% add additional fish food as requested by customer, to cart.
% @param Quantity (IN) - if 0, no action. If not 0, then add to cart
% and assert it back into the factbase.
add_fish_food(0) :- !.
add_fish_food(Quantity) :-
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  number_of_food(Cart, NumberOfFood),
  number_of_free_food(Cart, NumberOfFreeFood),
  number_of_tank(Cart, NumberOfTank),
  discount(Cart, Discount),
  NewFood is NumberOfFood + Quantity,
  append([], [NumberOfFish, NewFood, NumberOfFreeFood, NumberOfTank, Discount], NewCart),
  retract(cart(Cart)),
  assert(cart(NewCart)).

% ask customer if he wants to buy a fish tank. Only ask the question
% if the number of fish are > 10 and customer doesn't already have a
% tank in his shopping cart.
% @param Yorn (OUT) - populated as a result of this method.
ask_add_fish_tank(Yorn) :-
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  number_of_tank(Cart, NumberOfTank),
  ask_add_fish_tank(NumberOfFish, NumberOfTank, Yorn).
ask_add_fish_tank(NumberOfFish, _, Yorn) :-
  NumberOfFish < 10, Yorn is 0, !.
ask_add_fish_tank(_, NumberOfTank, Yorn) :-
  NumberOfTank > 0, Yorn is 0, !.
ask_add_fish_tank(_, _, Yorn) :-
  write('>> Add a fish tank? [y/n] '), read(Yorn), nl.

% Adds a fish tank to the cart and asserts it into the factbase.
% This is only done if the answer to the 'add a fish tank' question
% is 'y'.
add_fish_tank('y') :-
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  number_of_food(Cart, NumberOfFood),
  number_of_free_food(Cart, NumberOfFreeFood),
  discount(Cart, Discount),
  Quantity is 1,
  append([], [NumberOfFish, NumberOfFood, NumberOfFreeFood, Quantity, Discount], NewCart),
  retract(cart(Cart)),
  assert(cart(NewCart)).
add_fish_tank(_) :- !.

% if order value > $50, apply 10% discount on total
apply_discount :-
  cart(Cart),
  compute_cart_total(Cart, CartTotal),
  apply_discount(CartTotal).
apply_discount(CartTotal) :-
  CartTotal < 50, !.
apply_discount(CartTotal) :-
  CartTotal >= 50,
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  number_of_food(Cart, NumberOfFood),
  number_of_free_food(Cart, NumberOfFreeFood),
  number_of_tank(Cart, NumberOfTank),
  Discount is 0.1 * CartTotal,
  append([], [NumberOfFish, NumberOfFood, NumberOfFreeFood, NumberOfTank, Discount], NewCart),
  retract(cart(Cart)),
  assert(cart(NewCart)).

% display contents of the cart.
% @param Heading (IN) - legend for the cart display
% @param Cart (IN) - the sequence representing the cart.
display_cart(Heading) :-
  cart(Cart),
  number_of_fish(Cart, NumberOfFish),
  number_of_food(Cart, NumberOfFood),
  number_of_free_food(Cart, NumberOfFreeFood),
  number_of_tank(Cart, NumberOfTank),
  discount(Cart, Discount),
  compute_cart_total(Cart, CartTotal),
  write('==== '), write(Heading), write(' ===='), nl,
  write('#-Fish (@ $5/ea) = '), write(NumberOfFish), nl,
  write('#-Food (@ $2/ea) = '), write(NumberOfFood),
    write(' (Free: '), write(NumberOfFreeFood), write(')'), nl,
  write('#-Tank= (@ $40/ea) = '), write(NumberOfTank), nl,
  write('- Discount Given = ($'), write(Discount), write(')'), nl,
  Total is CartTotal - Discount,
  write('** Total = $'), write(Total), write(' **'), nl.

% The top level goal that is called from the client. It will prompt
% for the cart to be entered as a sequence, and print the invoice
% after applying all the rules to the shopping cart.
checkout :-
  retractall(cart(_)),
  write('Enter cart: '), read(Cart),
  assert(cart(Cart)),
  add_free_fish_food,
  ask_add_fish_food(MoreFishFood),
  add_fish_food(MoreFishFood),
  ask_add_fish_tank(AddFishTank),
  add_fish_tank(AddFishTank),
  apply_discount,
  display_cart('Invoice'),
  !.
