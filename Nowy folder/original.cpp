/*
 * Simulator.cpp
 *
 *  Created on: 14.05.2017
 *      Author: oramus
 */

#include "Simulator.h"
#include <stdlib.h>
#include "Common.h"
#include <limits.h>
#include <math.h>
#include <time.h>
#include "Helper.h"
#include <iostream>

using namespace std;

Simulator::Simulator() {
	srand48_r(time(NULL), &randBuffer);
	stepNumber = 0;
}

Simulator::~Simulator() {
}

void Simulator::moveRover(Possibilities option, unsigned int rover,
		unsigned int step) {
	if (option == UP) {
		roverPositionY[rover] += step;
		return;
	}
	if (option == DOWN) {
		roverPositionY[rover] -= step;
		return;
	}
	if (option == LEFT) {
		roverPositionX[rover] -= step;
		return;
	}
	if (option == RIGHT) {
		roverPositionX[rover] += step;
		return;
	}
	if (option == BACK_TO_START) {
		roverPositionX[rover] = 0;
		roverPositionY[rover] = 0;
	}
}

// TODO zrownoleglic
void Simulator::simulate(unsigned int steps) {
	double rnd;
	for (unsigned int step = 0, step_length = steps; step < steps;
			step++, step_length--) {
		stepNumber++;
		for (unsigned int rover = 0; rover < rovers; rover++) {
			drand48_r(&randBuffer, &rnd);
			moveRover(dm->getDecision(rover, stepNumber, rnd), rover,
					step_length);
		}
	}
}

// TODO zrownoleglic
void Simulator::calcStatistics() {
	maxDistance = 0;
	minDistance = ULONG_MAX;
	double sum = 0;
	double distance;
	for (unsigned int rover = 0; rover < rovers; rover++) {
		distance = sqrt(
				roverPositionX[rover] * roverPositionX[rover]
						+ roverPositionY[rover] * roverPositionY[rover]);

		if (distance < minDistance) {
			minDistance = distance;
		}
		if (distance > maxDistance) {
			maxDistance = distance;
		}
		sum += distance;
	}
	avgDistance = sum / rovers;
}

unsigned long Simulator::getManhattanDistance(unsigned int rover1,
		unsigned int rover2) {
	return Helper::getManhattanDistance(roverPositionX[rover1],
			roverPositionX[rover2], roverPositionY[rover1],
			roverPositionY[rover2]);
}

// TODO zrownoleglic
void Simulator::calcHistogram(unsigned int *histogram, unsigned long size,
		unsigned long scale) {
	unsigned long index;
	for (unsigned int r1 = 0; r1 < rovers; r1++) {
		for (unsigned int r2 = 0; r2 < r1; r2++) {
			index = getManhattanDistance(r1, r2) / scale;
			if (index < size) {
				histogram[index]++;
			}
		}

	}
}
