## BDS Project - Detecting Misinformation

Semester long project.

## Folder Structure

The workspace contains four folders by default, where:

- `src`: the folder to maintain sources - *App*.java is there
- `data`: the folder to maintain input data
- `output`: the folder to maintain output results
- `rapidminer_src`: the folder containing Rapidminer models

## Running the App
First build the Java Maven project using the POM file and the src files (keyword_extract.java, keyword_select.java)

Running Keyword Extract
- java -jar [jar file] [fake_file] [true_fale]
- fake_file: Fake_500.csv Fake_eval.csv
- true_file: True_500.csv True_eval.csv
- output: posLabels.csv, nerLabels.csv, nouns.csv, nerner.csv, adj_nn.csv, pron_vb.csv, adv_vb.csv

Running Keyword Select
- java -jar [jar file] [Fake.csv] [True.csv]
- fake_file: Fake_500.csv Fake_eval.csv
- true_file: True_500.csv True_eval.csv
- output: select_result.csv

RapidMiner
- Input: select_result.csv

## GitHub Link
- In case the original folder is needed
- https://github.com/YinWang3026/bds_project
