/*
 * @file	mazda_miata_vvt.cpp
 *
 * set_engine_type 47
 *
 * @date Oct 4, 2016
 * @author Andrey Belomutskiy, (c) 2012-2016
 * http://rusefi.com/forum/viewtopic.php?f=3&t=1095
 */

#include "mazda_miata_vvt.h"
#include "custom_engine.h"

EXTERN_ENGINE;

void setMazdaMiata2003EngineConfiguration(DECLARE_ENGINE_PARAMETER_F) {
	setCustomEngineConfiguration(PASS_ENGINE_PARAMETER_F);

	engineConfiguration->hasMapSensor = true;

	engineConfiguration->trigger.type = TT_MIATA_VVT;
	engineConfiguration->specs.displacement = 1.8;

	engineConfiguration->specs.cylindersCount = 4;
	engineConfiguration->specs.firingOrder = FO_1_3_4_2;

}

