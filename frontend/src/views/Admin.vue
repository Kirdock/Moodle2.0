<template>
  <div class="admin">
      <router-link to="/Admin/UserManagement" class="nav-link" v-if="$store.getters.userInfo.isAdmin" >{{ $t('user.management') }}</router-link>
      <router-link to="/Admin/CourseManagement" class="nav-link" >{{ $t('course.management') }}</router-link>
      <router-link to="/Admin/SemesterManagement" class="nav-link" v-if="$store.getters.userInfo.isAdmin" >{{ $t('semester.create') }}</router-link>
  </div>
</template>