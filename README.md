# horizonBN (WORK IN PROGRESS)
**horizonBN** is an open-source Java library for Bayesian Network structure learning, with a special focus on high-dimensional problems. It provides a robust implementation of several novel algorithms designed to scale efficiently and discover more accurate network structures.

This repository contains the official implementation of the research conducted during the PhD thesis of J.D. Laborda.

## Key Features
* 🧠 High-Dimensional Focus: Algorithms are specifically designed to handle datasets with a large number of variables.

* 🚀 Scalable & Parallel: Employs parallel and distributed paradigms to drastically reduce computation time.

* 🧩 Modular Design: Built with a clean, multi-module Maven architecture for easy maintenance and extension.

* 📚 Academic Foundation: Based on peer-reviewed research, making it ideal for academic and scientific use.

---

## Implemented Algorithms
The core contributions of `horizonBN` include:

* **PGES & CGES**: Parallel and distributed versions of the Greedy Equivalence Search (GES) algorithm, using a divide-and-conquer paradigm for scalability. These algorithms leverage the ConsensusBN library for merging subnetworks.

* **MCTSBN & PMCTSBN**: Innovative approaches that leverage Monte Carlo Tree Search (MCTS) to intelligently guide the search for optimal ancestral orderings, improving the quality of the learned structures.

---

## Project Structure
The project is organized into a multi-module Maven structure to ensure a clear separation of concerns.
```
horizonBN/
├── core-ges/       # Core logic for GES-based algorithms
├── core-mcts/      # Core logic for MCTS-based algorithms
├── pges/           # Implementation of the PGES algorithm
├── cges/           # Implementation of the CGES algorithms (RGES, SRGES, SBGES)
├── mctsbn/         # Implementation of the MCTSBN algorithm
└── pmctsbn/        # Implementation of the PMCTSBN algorithm
```
---

## Getting Started
### Installation
(Work in Progress)
`horizonBN` is available on Maven Central. To use it in your own Maven project, add the desired module as a dependency in your pom.xml.

For example, to use the PGES algorithm, add the following:
```XML
<dependency>
    <groupId>io.github.your-username</groupId>
    <artifactId>pges</artifactId>
    <version>1.0.0</version> 
</dependency>
```

### Basic Usage
Here is a conceptual example of how to use one of the learning algorithms from the library.

```Java

import io.github.your_username.horizon.data.DataSet;
import io.github.your_username.horizon.pges.PGESLearner;
import io.github.your_username.horizon.structure.BayesianNetwork;

public class Example {

    public static void main(String[] args) {
        // 1. Load your high-dimensional dataset
        DataSet data = new DataSet("path/to/your/data.csv");

        // 2. Configure the learning algorithm
        PGESLearner learner = new PGESLearner();
        learner.setNumberOfThreads(8); // Configure parallelism
        // ... set other parameters

        // 3. Learn the network structure
        System.out.println("Starting structure learning...");
        BayesianNetwork learnedNetwork = learner.learn(data);

        // 4. Print or save the resulting network
        System.out.println("Learning complete!");
        System.out.println("Learned Edges: " + learnedNetwork.getEdges());
    }
}
```
## How to Cite
If you use horizonBN in your research, please cite the relevant publications:

### For PGES:

```
@article{LABORDA2024111840,
title = {Parallel structural learning of Bayesian networks: Iterative divide and conquer algorithm based on structural fusion},
journal = {Knowledge-Based Systems},
volume = {296},
pages = {111840},
year = {2024},
issn = {0950-7051},
doi = {https://doi.org/10.1016/j.knosys.2024.111840},
url = {https://www.sciencedirect.com/science/article/pii/S095070512400474X},
author = {Jorge D. Laborda and Pablo Torrijos and José M. Puerta and José A. Gámez},
keywords = {Bayesian network learning, Bayesian network fusion/aggregation, Distributed machine learning, High-dimensional problems}
}
```

### For CGES:

```
@article{LABORDA2024109302,
title = {Distributed fusion-based algorithms for learning high-dimensional Bayesian Networks: Testing ring and star topologies},
journal = {International Journal of Approximate Reasoning},
volume = {175},
pages = {109302},
year = {2024},
issn = {0888-613X},
doi = {https://doi.org/10.1016/j.ijar.2024.109302},
url = {https://www.sciencedirect.com/science/article/pii/S0888613X24001890},
author = {Jorge D. Laborda and Pablo Torrijos and José M. Puerta and José A. Gámez},
keywords = {Bayesian Network learning, Bayesian Network fusion/aggregation, Distributed machine learning}
```

### For MCTSBN:

```
@InProceedings{10.1007/978-3-031-74003-9_32,
author="Laborda, Jorge D.
and Torrijos, Pablo
and Puerta, Jos{\'e} M.
and G{\'a}mez, Jos{\'e} A.",
editor="Lesot, Marie-Jeanne
and Vieira, Susana
and Reformat, Marek Z.
and Carvalho, Jo{\~a}o Paulo
and Batista, Fernando
and Bouchon-Meunier, Bernadette
and Yager, Ronald R.",
title="Enhancing Bayesian Network Structural Learning with Monte Carlo Tree Search",
booktitle="Information Processing and Management of Uncertainty in Knowledge-Based Systems",
year="2024",
publisher="Springer Nature Switzerland",
address="Cham",
pages="403--414"
}
```

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
