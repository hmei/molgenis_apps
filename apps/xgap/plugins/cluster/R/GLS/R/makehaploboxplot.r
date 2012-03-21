#
# MakeHaploBoxplot.R
# - Description: Plot routine for haplotypes
#
# Copyright (c) 2012 Willem Kruijer and Danny Arends

MakeHaploBoxplot <- function(data.vector, hap.vector, file.name) {
  plot.data <- data.frame(trait.value = as.numeric(data.vector), hap.vector = as.integer(hap.vector))
  if(!missing(file.name)) jpeg(file.name,quality=100)
  boxplot(trait.value ~ hap.vector,data=plot.data,names=as.character(tabulate(hap.vector)))
  if(!missing(file.name)) dev.off()
}
