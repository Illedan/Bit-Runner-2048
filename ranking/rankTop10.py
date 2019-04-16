#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Mar 23 08:17:40 2019

@author: pb4
"""

import pandas as pd
from scipy.optimize import minimize

# Read data
data = pd.read_csv('top10winrates.txt', delim_whitespace=True)
names = list(set(list(data['Name1'])+list(data['Name2'])))
name_indice = dict(zip(names, range(len(names))))


def likelihood(x):
    # Compute likelihood of candidate score vector x
    #
    # If a player has score s1, and another has score s2, we consider in our
    # model that player s1 has a theoretical winrate of (s1 / (s1 + s2)) against
    # player 2
    #
    # Given a score vector x, this function calculates the average error made
    # by the model when compared to the actual observations in "data"
    #
    # The objective is to find the vector "x" that minimizes this function
    ret = 0
    for index, row in data.iterrows():
        expected = x[name_indice[row['Name1']]] / (x[name_indice[row['Name1']]] + x[name_indice[row['Name2']]])
        ret += abs(100*expected - row['Win_%'])
    return ret


# Use scipy optimization tools to calculate most likely scores for each player
res = minimize(likelihood,[1]*len(names), bounds=[(0.1, 1000)]*len(names), options={'maxiter':1000})

# Normalize so that the best player has score 1000
res.x /= max(res.x)
res.x *= 1000

# Display the results
result = sorted(list(zip(res.x, names)))
print('Player scores:')
for score, name in result:
    print(name, '    ', int(score))
print()