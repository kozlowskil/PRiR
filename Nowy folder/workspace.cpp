// TODO zrownoleglic
// void Simulator::simulate(unsigned int steps) {
	// double rnd;
	// srand48_r(time(NULL), &randBuffer);
	// for (unsigned int step = 0, step_length = steps; step < steps;
			// step++, step_length--) {
		// stepNumber++;		
		// #pragma omp parallel for schedule (dynamic)	
		// for (unsigned int rover = 0; rover < rovers; rover++) {
			// drand48_r(&randBuffer, &rnd);
			// moveRover(dm->getDecision(rover, stepNumber, rnd), rover,
					// step_length);
		// }
	// }
// }



void Simulator::simulate(unsigned int steps) {
	cout << "Wejscie do SIMULATE" << endl;
	#pragma omp parallel 
	{
		double rnd;
		srand48_r(time(NULL), &randBuffer);
		int id = omp_get_thread_num();
		cout << "Thread: " << id << " mam wartosc rand: " << &randBuffer << "lub z &: " << endl;
		#pragma omp parallel for schedule (guided)
		for (unsigned int rover = 0; rover < rovers; rover++) {
			for (unsigned int step = 0, step_length = steps; step < steps;
				step++, step_length--) {
				stepNumber++;	
				drand48_r(&randBuffer, &rnd);
				moveRover(dm->getDecision(rover, stepNumber, rnd), rover,
						step_length);
			}
		}
	}
	cout << "Wyjscie z SIMULATE" << endl;
}

